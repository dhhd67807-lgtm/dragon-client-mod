#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import re
import shutil
from dataclasses import dataclass
from pathlib import Path
from typing import Dict, List, Optional, Tuple

ROOT = Path(__file__).resolve().parents[1]
SKINS_DIR = ROOT / "skins"
VERSIONS_DIR = ROOT / "versions"

CATEGORY_ORDER = ["SWORD", "PICKAXE", "AXE"]

SKIP_TOKENS = {
    "_pulling_",
    "_noanim",
    "_blocking",
    "arrow",
    "wings",
    "crown",
    "shield",
    "helmet",
    "hoe",
    "shovel",
    "armor",
    "leggings",
    "chestplate",
    "boots",
    "desktop",
    "layer_",
    "particles",
}


@dataclass(frozen=True)
class SourceModel:
    namespace: str
    stem: str
    source_path: Path
    rel_under_models: Path


@dataclass(frozen=True)
class SourceTexture:
    namespace: str
    source_path: Path
    rel_under_textures: Path


@dataclass(frozen=True)
class SkinEntry:
    category: str
    label: str
    alias: str
    namespace: str
    stem: str


def infer_namespace_and_rel(path: Path, anchor: str) -> Optional[Tuple[str, Path]]:
    parts = list(path.parts)
    if "resourcepack" in parts and "assets" in parts:
        assets_idx = parts.index("assets")
        if assets_idx + 1 >= len(parts):
            return None
        namespace = parts[assets_idx + 1]
        try:
            anchor_idx = parts.index(anchor, assets_idx + 2)
        except ValueError:
            return None
        rel = Path(*parts[anchor_idx + 1 :])
        return namespace, rel

    try:
        anchor_idx = parts.index(anchor)
    except ValueError:
        return None

    if anchor_idx <= 0:
        return None

    namespace = parts[anchor_idx - 1]
    rel = Path(*parts[anchor_idx + 1 :])
    return namespace, rel


def classify_category(stem: str) -> Optional[str]:
    s = stem.lower()
    if any(token in s for token in SKIP_TOKENS):
        return None
    if "pickaxe" in s:
        return "PICKAXE"
    if "axe" in s and "pickaxe" not in s:
        return "AXE"
    if "bow" in s:
        return None
    return "SWORD"


def title_from_stem(stem: str) -> str:
    clean = stem
    if clean.endswith("_dc"):
        clean = clean[:-3]
    return clean.replace("_", " ").strip().title()


def discover_source_assets() -> Tuple[Dict[Tuple[str, str], SourceModel], List[SourceTexture]]:
    models: Dict[Tuple[str, str], SourceModel] = {}
    textures: List[SourceTexture] = []

    for json_file in SKINS_DIR.rglob("*.json"):
        ns_rel = infer_namespace_and_rel(json_file, "models")
        if not ns_rel:
            continue
        namespace, rel = ns_rel
        stem = json_file.stem
        key = (namespace, stem)

        # Prefer resourcepack/assets source if duplicates exist.
        if key not in models or "resourcepack" in str(json_file):
            models[key] = SourceModel(
                namespace=namespace,
                stem=stem,
                source_path=json_file,
                rel_under_models=rel,
            )

    for file in SKINS_DIR.rglob("*"):
        if not file.is_file():
            continue
        ns_rel = infer_namespace_and_rel(file, "textures")
        if not ns_rel:
            continue
        namespace, rel = ns_rel
        textures.append(
            SourceTexture(
                namespace=namespace,
                source_path=file,
                rel_under_textures=rel,
            )
        )

    return models, textures


def parse_json_file(path: Path) -> Optional[dict]:
    try:
        with path.open("r", encoding="utf-8") as f:
            return json.load(f)
    except Exception:
        return None


def extract_ns_stem_from_wrapper(wrapper_path: Path) -> Optional[Tuple[str, str]]:
    data = parse_json_file(wrapper_path)
    if not isinstance(data, dict):
        return None

    parent = data.get("parent")
    if isinstance(parent, str):
        m = re.match(r"^([a-z0-9_.-]+):(.+)$", parent)
        if m:
            ns = m.group(1)
            model_path = m.group(2)
            stem = model_path.split("/")[-1]
            return ns, stem

    textures = data.get("textures")
    if isinstance(textures, dict):
        layer0 = textures.get("layer0")
        if isinstance(layer0, str):
            m = re.match(r"^([a-z0-9_.-]+):(.+)$", layer0)
            if m:
                ns = m.group(1)
                tex_path = m.group(2)
                stem = tex_path.split("/")[-1]
                return ns, stem

    return None


def collect_existing_wrapper_aliases(reference_version: Path) -> Dict[Tuple[str, str], str]:
    alias_map: Dict[Tuple[str, str], str] = {}
    wrappers_dir = reference_version / "src/main/resources/assets/minecraft/models/item"
    if not wrappers_dir.exists():
        return alias_map

    for wrapper in wrappers_dir.glob("dc_*.json"):
        key = extract_ns_stem_from_wrapper(wrapper)
        if key is None:
            continue
        alias_map[key] = wrapper.stem

    # Keep explicit legacy aliases used in cards.
    alias_map.setdefault(("heartflame", "heartflame_sword"), "heartflame_sword_dc")
    alias_map.setdefault(("heartflame", "heartflame_pickaxe"), "heartflame_pickaxe_dc")
    alias_map.setdefault(("heartflame", "heartflame_axe"), "heartflame_axe_dc")

    return alias_map


def sanitize_alias(text: str) -> str:
    s = re.sub(r"[^a-z0-9_]+", "_", text.lower())
    s = re.sub(r"_+", "_", s).strip("_")
    return s


def propose_alias(namespace: str, stem: str, used_aliases: set[str]) -> str:
    if namespace == "heartflame" and stem in {"heartflame_sword", "heartflame_pickaxe", "heartflame_axe"}:
        base = f"{stem}_dc"
    else:
        base = f"dc_{sanitize_alias(stem)}"

    alias = base
    if alias in used_aliases:
        alias = f"dc_{sanitize_alias(namespace)}_{sanitize_alias(stem)}"

    i = 2
    while alias in used_aliases:
        alias = f"{base}_{i}"
        i += 1

    return alias


def normalize_model_json_for_item_textures(model_data: object, namespace: str) -> object:
    if not isinstance(model_data, dict):
        return model_data

    textures = model_data.get("textures")
    if isinstance(textures, dict):
        for key, value in list(textures.items()):
            if isinstance(value, str) and value.startswith(f"{namespace}:"):
                rest = value.split(":", 1)[1]
                if "/" not in rest:
                    textures[key] = f"{namespace}:item/{rest}"

    return model_data


def write_json(path: Path, data: object) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", encoding="utf-8") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


def copy_assets_to_version(version_dir: Path, models: Dict[Tuple[str, str], SourceModel], textures: List[SourceTexture]) -> None:
    resources = version_dir / "src/main/resources"

    for model in models.values():
        raw = parse_json_file(model.source_path)
        model_data = normalize_model_json_for_item_textures(raw, model.namespace)

        # Preserve source relative placement.
        dst_original = resources / "assets" / model.namespace / "models" / model.rel_under_models
        write_json(dst_original, model_data)

        # Also guarantee item-path variant for wrapper parents.
        dst_item = resources / "assets" / model.namespace / "models" / "item" / f"{model.stem}.json"
        write_json(dst_item, model_data)

    for tex in textures:
        dst_original = resources / "assets" / tex.namespace / "textures" / tex.rel_under_textures
        dst_original.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(tex.source_path, dst_original)

        # Mirror to textures/item/<basename> for item texture ids.
        dst_item = resources / "assets" / tex.namespace / "textures" / "item" / tex.source_path.name
        dst_item.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(tex.source_path, dst_item)


def parse_option_blocks(java_text: str) -> Dict[str, List[Tuple[str, str]]]:
    result: Dict[str, List[Tuple[str, str]]] = {}
    for category in CATEGORY_ORDER:
        pattern = re.compile(
            rf'OPTIONS\.put\(Category\.{category}, new SkinOption\[\] \{{\n(.*?)\n\s*\}}\);',
            re.DOTALL,
        )
        m = pattern.search(java_text)
        entries: List[Tuple[str, str]] = []
        if m:
            body = m.group(1)
            for em in re.finditer(r'new SkinOption\("([^"]+)",\s*"([^"]+)"\),?', body):
                entries.append((em.group(1), em.group(2)))
        result[category] = entries
    return result


def render_option_block(category: str, entries: List[Tuple[str, str]]) -> str:
    lines = [f"        OPTIONS.put(Category.{category}, new SkinOption[] {{"]
    for label, model_id in entries:
        escaped_label = label.replace('"', '\\"')
        escaped_model = model_id.replace('"', '\\"')
        lines.append(f'            new SkinOption("{escaped_label}", "{escaped_model}"),')
    lines.append("        });")
    return "\n".join(lines)


def upsert_option_blocks(java_text: str, merged: Dict[str, List[Tuple[str, str]]]) -> str:
    out = java_text
    for category in CATEGORY_ORDER:
        block = render_option_block(category, merged.get(category, []))
        pattern = re.compile(
            rf'\s*OPTIONS\.put\(Category\.{category}, new SkinOption\[\] \{{\n.*?\n\s*\}}\);',
            re.DOTALL,
        )
        out, count = pattern.subn("\n" + block, out, count=1)
        if count == 0:
            raise RuntimeError(f"Could not find OPTIONS block for {category}")
    return out


def is_model_id_manager(java_path: Path) -> bool:
    text = java_path.read_text(encoding="utf-8")
    return "DataComponentTypes.ITEM_MODEL" in text and "new SkinOption(String label, String modelPath)" in text


def discover_versions() -> List[Path]:
    versions: List[Path] = []
    for version in sorted(VERSIONS_DIR.glob("*-fabric")):
        gsm = version / "src/main/java/com/dragonclient/cosmetics/GearSkinManager.java"
        if gsm.exists():
            versions.append(version)
    return versions


def build_entries(
    models: Dict[Tuple[str, str], SourceModel],
    alias_map: Dict[Tuple[str, str], str],
) -> List[SkinEntry]:
    used_aliases = set(alias_map.values())
    entries: List[SkinEntry] = []

    for (namespace, stem), model in sorted(models.items(), key=lambda x: (x[0][0], x[0][1])):
        category = classify_category(stem)
        if category is None:
            continue

        alias = alias_map.get((namespace, stem))
        if not alias:
            alias = propose_alias(namespace, stem, used_aliases)
            alias_map[(namespace, stem)] = alias
            used_aliases.add(alias)

        entries.append(
            SkinEntry(
                category=category,
                label=title_from_stem(stem),
                alias=alias,
                namespace=namespace,
                stem=stem,
            )
        )

    return entries


def ensure_wrappers(version_dir: Path, entries: List[SkinEntry]) -> int:
    wrappers_dir = version_dir / "src/main/resources/assets/minecraft/models/item"
    wrappers_dir.mkdir(parents=True, exist_ok=True)
    created = 0

    for e in entries:
        wrapper_path = wrappers_dir / f"{e.alias}.json"
        if wrapper_path.exists():
            continue
        data = {"parent": f"{e.namespace}:item/{e.stem}"}
        write_json(wrapper_path, data)
        created += 1

    return created


def update_gear_skin_manager(version_dir: Path, discovered_entries: List[SkinEntry]) -> Tuple[int, int]:
    java_path = version_dir / "src/main/java/com/dragonclient/cosmetics/GearSkinManager.java"
    if not java_path.exists() or not is_model_id_manager(java_path):
        return 0, 0

    text = java_path.read_text(encoding="utf-8")
    existing = parse_option_blocks(text)

    existing_model_ids = {model_id for entries in existing.values() for _, model_id in entries}

    merged = {k: list(v) for k, v in existing.items()}
    added = 0

    for e in discovered_entries:
        if e.alias in existing_model_ids:
            continue
        merged.setdefault(e.category, []).append((e.label, e.alias))
        existing_model_ids.add(e.alias)
        added += 1

    updated_text = upsert_option_blocks(text, merged)
    if updated_text != text:
        java_path.write_text(updated_text, encoding="utf-8")
        return added, 1
    return added, 0


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Auto-sync Dragon gear skins: copy assets, create wrappers, and update skin cards."
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Show what would be changed without writing files.",
    )
    args = parser.parse_args()

    if not SKINS_DIR.exists():
        raise SystemExit(f"skins directory not found: {SKINS_DIR}")

    versions = discover_versions()
    if not versions:
        raise SystemExit("No version directories with GearSkinManager found.")

    # Use 1.21.10 as alias reference when available.
    reference = next((v for v in versions if v.name == "1.21.10-fabric"), versions[0])

    models, textures = discover_source_assets()
    alias_map = collect_existing_wrapper_aliases(reference)
    entries = build_entries(models, alias_map)

    if args.dry_run:
        print(f"[dry-run] versions: {len(versions)}")
        print(f"[dry-run] source models: {len(models)}")
        print(f"[dry-run] source textures: {len(textures)}")
        print(f"[dry-run] discovered card entries: {len(entries)}")
        for c in CATEGORY_ORDER:
            count = sum(1 for e in entries if e.category == c)
            print(f"[dry-run]  {c}: {count}")
        return 0

    total_wrappers = 0
    managers_updated = 0
    options_added = 0

    for version in versions:
        copy_assets_to_version(version, models, textures)
        total_wrappers += ensure_wrappers(version, entries)

        added, touched = update_gear_skin_manager(version, entries)
        options_added += added
        managers_updated += touched

    print("Gear skin auto-sync complete")
    print(f"Versions: {len(versions)}")
    print(f"Source models copied: {len(models)}")
    print(f"Source textures copied: {len(textures)}")
    print(f"Discovered entries: {len(entries)}")
    print(f"Wrappers created: {total_wrappers}")
    print(f"GearSkinManager files updated: {managers_updated}")
    print(f"New card options added: {options_added}")
    print("Command: python3 scripts/auto_sync_gear_skins.py")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
