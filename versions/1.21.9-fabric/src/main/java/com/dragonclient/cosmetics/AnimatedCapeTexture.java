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

                List<NativeImage> frames = new ArrayList<>(frameCount);
                List<Integer> delaysMs = new ArrayList<>(frameCount);

                for (int i = 0; i < frameCount; i++) {
                    BufferedImage frame = reader.read(i);
                    IIOMetadata metadata = reader.getImageMetadata(i);
                    frames.add(toNativeImage(frame));
                    delaysMs.add(readDelayMs(metadata));
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
        if (target == null || target.getWidth() != frame.getWidth() || target.getHeight() != frame.getHeight()) {
            texture.setImage(copyImage(frame));
        } else {
            target.copyFrom(frame);
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
