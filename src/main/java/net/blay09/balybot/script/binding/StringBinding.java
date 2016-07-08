package net.blay09.balybot.script.binding;

import org.apache.commons.lang3.StringUtils;

public class StringBinding {

    public String join(String[] arr, String delimiter) {
        return StringUtils.join(arr, delimiter);
    }

}
