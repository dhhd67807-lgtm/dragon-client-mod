package com.dragonclient.cosmetics;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class AnimatedCapeTexture {
    private static final int DEFAULT_DELAY_MS = 100;

    private final NativeImageBackedTexture texture;
    private final List<NativeImage> frames;
    private final List<Integer> delaysMs;
    private int currentFrame = 0;
    private long nextFrameAtMs;

    private AnimatedCapeTexture(NativeImageBackedTexture texture, List<NativeImage> frames, List<Integer> delaysMs) {
        this.texture = texture;
        this.frames = frames;
        this.delaysMs = delaysMs;
        this.nextFrameAtMs = System.currentTimeMillis() + delaysMs.get(0);
    }

    public static AnimatedCapeTexture load(Identifier textureId, String debugName, String resourcePath) {
        InputStream resourceStream = AnimatedCapeTexture.class.getClassLoader().getResourceAsStream(resourcePath);
        if (resourceStream == null) {
            System.err.println("[DragonClient] Missing animated cape resource: " + resourcePath);
            return null;
        }

        try (InputStream stream = resourceStream; ImageInputStream imageStream = ImageIO.createImageInputStream(stream)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageStream);
            if (!readers.hasNext()) {
                System.err.println("[DragonClient] No GIF reader available for: " + resourcePath);
                return null;
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageStream, false, false);

                int frameCount = reader.getNumImages(true);
                if (frameCount <= 0) {
                    System.err.println("[DragonClient] Animated cape has no frames: " + resourcePath);
                    return null;
                }

                BufferedImage firstFrame = reader.read(0);
                int[] logicalScreenSize = readLogicalScreenSize(reader.getStreamMetadata(), firstFrame.getWidth(), firstFrame.getHeight());
                int canvasWidth = logicalScreenSize[0];
                int canvasHeight = logicalScreenSize[1];

                List<NativeImage> frames = new ArrayList<>(frameCount);
                List<Integer> delaysMs = new ArrayList<>(frameCount);
                BufferedImage composedFrame = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);

                for (int i = 0; i < frameCount; i++) {
                    BufferedImage frame = i == 0 ? firstFrame : reader.read(i);
                    IIOMetadata metadata = reader.getImageMetadata(i);
                    int left = readImageLeft(metadata);
                    int top = readImageTop(metadata);
                    String disposalMethod = readDisposalMethod(metadata);

                    BufferedImage previousComposed = null;
                    if ("restoreToPrevious".equals(disposalMethod)) {
                        previousComposed = copyBufferedImage(composedFrame);
                    }

                    Graphics2D graphics = composedFrame.createGraphics();
                    graphics.setComposite(AlphaComposite.SrcOver);
                    graphics.drawImage(frame, left, top, null);
                    graphics.dispose();

                    frames.add(toNativeImage(composedFrame));
                    delaysMs.add(readDelayMs(metadata));

                    if ("restoreToBackgroundColor".equals(disposalMethod)) {
                        clearRect(composedFrame, left, top, frame.getWidth(), frame.getHeight());
                    } else if ("restoreToPrevious".equals(disposalMethod) && previousComposed != null) {
                        composedFrame = previousComposed;
                    }
                }

                NativeImageBackedTexture texture = new NativeImageBackedTexture(() -> debugName, copyImage(frames.get(0)));
                net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
                if (client == null) {
                    texture.close();
                    closeImages(frames);
                    return null;
                }
                client.getTextureManager().registerTexture(textureId, texture);
                return new AnimatedCapeTexture(texture, frames, delaysMs);
            } finally {
                reader.dispose();
            }
        } catch (Exception e) {
            System.err.println("[DragonClient] Failed to load animated cape: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public void tick() {
        if (frames.size() <= 1) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now < nextFrameAtMs) {
            return;
        }

        currentFrame = (currentFrame + 1) % frames.size();
        NativeImage frame = frames.get(currentFrame);
        NativeImage target = texture.getImage();
        if (target == null) {
            texture.setImage(copyImage(frame));
        } else if (target.getWidth() == frame.getWidth() && target.getHeight() == frame.getHeight()) {
            target.copyFrom(frame);
        } else {
            // Keep texture dimensions stable to avoid upload crashes with mixed-size GIF frames.
            texture.setImage(fitImageToSize(frame, target.getWidth(), target.getHeight()));
        }
        texture.upload();
        nextFrameAtMs = now + delaysMs.get(currentFrame);
    }

    public void close() {
        texture.close();
        closeImages(frames);
    }

    private static void closeImages(List<NativeImage> images) {
        for (NativeImage image : images) {
            image.close();
        }
    }

    private static NativeImage toNativeImage(BufferedImage bufferedImage) {
        NativeImage image = new NativeImage(bufferedImage.getWidth(), bufferedImage.getHeight(), true);
        for (int y = 0; y < bufferedImage.getHeight(); y++) {
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                image.setColorArgb(x, y, bufferedImage.getRGB(x, y));
            }
        }
        return image;
    }

    private static NativeImage copyImage(NativeImage source) {
        NativeImage copy = new NativeImage(source.getWidth(), source.getHeight(), true);
        copy.copyFrom(source);
        return copy;
    }

    private static NativeImage fitImageToSize(NativeImage source, int width, int height) {
        NativeImage fitted = new NativeImage(width, height, true);
        int copyWidth = Math.min(width, source.getWidth());
        int copyHeight = Math.min(height, source.getHeight());

        for (int y = 0; y < copyHeight; y++) {
            for (int x = 0; x < copyWidth; x++) {
                fitted.setColorArgb(x, y, source.getColorArgb(x, y));
            }
        }
        return fitted;
    }

    private static int[] readLogicalScreenSize(IIOMetadata metadata, int fallbackWidth, int fallbackHeight) {
        if (metadata == null) {
            return new int[] { fallbackWidth, fallbackHeight };
        }

        try {
            Node root = metadata.getAsTree("javax_imageio_gif_stream_1.0");
            Node descriptor = findFirstNode(root, "LogicalScreenDescriptor");
            if (descriptor == null) {
                return new int[] { fallbackWidth, fallbackHeight };
            }
            NamedNodeMap attributes = descriptor.getAttributes();
            if (attributes == null) {
                return new int[] { fallbackWidth, fallbackHeight };
            }

            int width = parsePositiveInt(attributes.getNamedItem("logicalScreenWidth"), fallbackWidth);
            int height = parsePositiveInt(attributes.getNamedItem("logicalScreenHeight"), fallbackHeight);
            return new int[] { width, height };
        } catch (Exception ignored) {
            return new int[] { fallbackWidth, fallbackHeight };
        }
    }

    private static int readImageLeft(IIOMetadata metadata) {
        NamedNodeMap attributes = readImageDescriptorAttributes(metadata);
        return attributes == null ? 0 : parsePositiveInt(attributes.getNamedItem("imageLeftPosition"), 0);
    }

    private static int readImageTop(IIOMetadata metadata) {
        NamedNodeMap attributes = readImageDescriptorAttributes(metadata);
        return attributes == null ? 0 : parsePositiveInt(attributes.getNamedItem("imageTopPosition"), 0);
    }

    private static NamedNodeMap readImageDescriptorAttributes(IIOMetadata metadata) {
        if (metadata == null) {
            return null;
        }

        try {
            Node root = metadata.getAsTree("javax_imageio_gif_image_1.0");
            Node descriptor = findFirstNode(root, "ImageDescriptor");
            return descriptor == null ? null : descriptor.getAttributes();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String readDisposalMethod(IIOMetadata metadata) {
        if (metadata == null) {
            return "none";
        }

        try {
            Node root = metadata.getAsTree("javax_imageio_gif_image_1.0");
            Node gce = findFirstNode(root, "GraphicControlExtension");
            if (gce == null) {
                return "none";
            }
            NamedNodeMap attributes = gce.getAttributes();
            if (attributes == null) {
                return "none";
            }
            Node disposalNode = attributes.getNamedItem("disposalMethod");
            if (disposalNode == null) {
                return "none";
            }
            return disposalNode.getNodeValue();
        } catch (Exception ignored) {
            return "none";
        }
    }

    private static Node findFirstNode(Node node, String name) {
        if (node == null) {
            return null;
        }
        if (name.equals(node.getNodeName())) {
            return node;
        }

        Node child = node.getFirstChild();
        while (child != null) {
            Node found = findFirstNode(child, name);
            if (found != null) {
                return found;
            }
            child = child.getNextSibling();
        }
        return null;
    }

    private static int parsePositiveInt(Node node, int fallback) {
        if (node == null) {
            return fallback;
        }
        try {
            int parsed = Integer.parseInt(node.getNodeValue());
            return parsed > 0 ? parsed : fallback;
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    private static BufferedImage copyBufferedImage(BufferedImage source) {
        BufferedImage copy = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = copy.createGraphics();
        graphics.setComposite(AlphaComposite.Src);
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return copy;
    }

    private static void clearRect(BufferedImage image, int left, int top, int width, int height) {
        int startX = Math.max(0, left);
        int startY = Math.max(0, top);
        int endX = Math.min(image.getWidth(), left + width);
        int endY = Math.min(image.getHeight(), top + height);

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                image.setRGB(x, y, 0x00000000);
            }
        }
    }

    private static int readDelayMs(IIOMetadata metadata) {
        if (metadata == null) {
            return DEFAULT_DELAY_MS;
        }

        try {
            Node root = metadata.getAsTree("javax_imageio_gif_image_1.0");
            int delayMs = findDelayMs(root);
            return Math.max(delayMs, 20);
        } catch (Exception e) {
            return DEFAULT_DELAY_MS;
        }
    }

    private static int findDelayMs(Node node) {
        if (node == null) {
            return DEFAULT_DELAY_MS;
        }

        if ("GraphicControlExtension".equals(node.getNodeName())) {
            NamedNodeMap attributes = node.getAttributes();
            if (attributes != null) {
                Node delayNode = attributes.getNamedItem("delayTime");
                if (delayNode != null) {
                    try {
                        return Math.max(Integer.parseInt(delayNode.getNodeValue()) * 10, DEFAULT_DELAY_MS);
                    } catch (NumberFormatException ignored) {
                        return DEFAULT_DELAY_MS;
                    }
                }
            }
            return DEFAULT_DELAY_MS;
        }

        Node child = node.getFirstChild();
        while (child != null) {
            int delay = findDelayMs(child);
            if (delay != DEFAULT_DELAY_MS) {
                return delay;
            }
            child = child.getNextSibling();
        }
        return DEFAULT_DELAY_MS;
    }
}
