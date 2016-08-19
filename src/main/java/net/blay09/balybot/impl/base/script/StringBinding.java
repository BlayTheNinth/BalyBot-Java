package net.blay09.balybot.impl.base.script;

import org.apache.commons.lang3.StringUtils;

public class StringBinding {

    public String join(String[] arr, String delimiter) {
        return StringUtils.join(arr, delimiter);
    }

}
