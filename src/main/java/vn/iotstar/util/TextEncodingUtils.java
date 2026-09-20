package vn.iotstar.util;

import java.text.Normalizer;

import org.springframework.web.util.HtmlUtils;

public final class TextEncodingUtils {
    private TextEncodingUtils() {
    }

    public static String normalize(String value) {
        if (value == null) {
            return null;
        }

        String unescapedValue = HtmlUtils.htmlUnescape(value);
        return Normalizer.normalize(
                unescapedValue,
                Normalizer.Form.NFC);
    }
}