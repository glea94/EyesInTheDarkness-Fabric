package dev.gigaherz.eyes.config;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import org.slf4j.Logger;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class BiomeRules
{
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final AtomicReference<List<Rule>> rules = new AtomicReference<>(new ArrayList<>());

    /*package*/
    static void parseRules(List<? extends String> dimensionRules)
    {
        var list = new ArrayList<Rule>();
        dimensionRules.forEach(r -> {
            Rule parse = parse(r);
            if (parse != null)
                list.add(parse);
        });
        // Unlike the NeoForge build, there's no "forge:is_void" convention tag available here,
        // so there is no automatic trailing rule anymore. Add your own "!*" rule at the end of
        // BiomeRules in the config if you want spawning to default to disallowed.
        rules.set(list);
    }

    public static boolean isBiomeAllowed(ServerLevel level, Holder<Biome> key)
    {
        for (Rule rule : rules.get())
        {
            if (rule.test(level, key))
                return rule.allow;
        }
        return true;
    }

    @Nullable
    private static Rule parse(String rule)
    {
        boolean allow = true;
        if (rule.startsWith("!"))
        {
            allow = false;
            rule = rule.substring(1);
        }
        if (rule.startsWith("#") || rule.startsWith("$"))
        {
            return new Rule(allow, null, rule.substring(1));
        }
        else if (rule.equals("*"))
        {
            return new Rule(allow, null, null);
        }
        else
        {
            return new Rule(allow, rule, null);
        }
    }

    private static class Rule
    {
        public final boolean allow;
        public final Identifier registryName;
        public final TagKey<Biome> tagKey;

        private Rule(boolean allow, @Nullable String registryName, @Nullable String tagName)
        {
            this.allow = allow;
            this.registryName = registryName != null ? Identifier.parse(registryName) : null;
            this.tagKey = tagName != null ? TagKey.create(Registries.BIOME, Identifier.parse(tagName)) : null;
        }

        public boolean test(ServerLevel level, Holder<Biome> biome)
        {
            if (registryName != null)
            {
                Identifier name = biome.unwrap().map(
                        ResourceKey::identifier,
                        b -> level.registryAccess().lookupOrThrow(Registries.BIOME).getKey(b));
                return registryName.equals(name);
            }
            else if(tagKey != null)
            {
                return biome.is(tagKey);
            }
            else
            {
                return true;
            }
        }
    }
}
