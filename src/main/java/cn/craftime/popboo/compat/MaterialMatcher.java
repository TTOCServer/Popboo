package cn.craftime.popboo.compat;

import org.bukkit.Material;

public class MaterialMatcher {
    public boolean isBambooRelated(Material material) {
        return bambooReason(material) != null;
    }

    public String bambooReason(Material material) {
        if (material == null) return null;
        String name = material.name();
        if (name.contains("BAMBOO")) return "name_contains_bamboo";
        if ("SCAFFOLDING".equals(name)) return "whitelist_scaffolding";
        return null;
    }

    public boolean isBambooBlock(Material material) {
        return isBambooRelated(material);
    }

    public boolean isBambooFuel(Material material) {
        return isBambooRelated(material);
    }
}
