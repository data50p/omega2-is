package com.femtioprocent.omega.adm.assets;

import com.femtioprocent.omega.OmegaContext;
import com.femtioprocent.omega.lesson.machine.Target;
import com.femtioprocent.omega.util.SundryUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by lars on 2017-02-12.
 */
public class TargetCombinations {

    public static class TCItem {
        public String fn;
        public Boolean exist;
        public String originalExtention;

        public TCItem(String fn) {
            this(fn, null);
        }

        public TCItem(String fn, String originalExtention) {
            this.fn = fn;
            this.originalExtention = originalExtention;
            this.exist = OmegaContext.omegaAssetsExist(fn);

            if (fn.contains("{"))
                OmegaContext.serr_log.getLogger().info("MORE using var");
            if (fn.contains(","))
                OmegaContext.serr_log.getLogger().info("MORE using ,");
            if (!fn.contains("/"))
                OmegaContext.serr_log.getLogger().info("LESS using /");
        }

        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (!(o instanceof TCItem))
                return false;
            return ((TCItem) o).fn.equals(fn);
        }

        public int hashCode() {
            return fn.hashCode();
        }

        public String formatOriginalExtention() {
            return SundryUtils.empty(originalExtention) ? "·" : originalExtention;
        }
    }

    public static class Builder {
        List<TargetCombinations> bundle = new ArrayList<>();
        TargetCombinations as_one_cache = null;

        public Builder() {
        }

        public void add(TargetCombinations tc) {
            bundle.add(tc);
            as_one_cache = null;
        }

        public TargetCombinations asOne() {
            if (as_one_cache != null)
                return as_one_cache;

            TargetCombinations as_one = new TargetCombinations();
            for (TargetCombinations tc : bundle) {
                as_one.merge(tc);
            }
            as_one_cache = as_one;
            return as_one;
        }

        public int srcSize() {
            return asOne().src_set.size();
        }
    }

    public Set<TCItem> src_set = new HashSet<>();
    public Set<TCItem> dep_set = new HashSet<>();
    public Set<Target> tg_set = new HashSet<>();

    private void merge(TargetCombinations tc) {
        dep_set.addAll(tc.dep_set);
        src_set.addAll(tc.src_set);
    }
}
