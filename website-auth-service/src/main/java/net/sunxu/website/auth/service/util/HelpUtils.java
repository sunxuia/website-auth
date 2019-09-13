package net.sunxu.website.auth.service.util;

import java.util.Random;
import lombok.experimental.UtilityClass;

@UtilityClass
public class HelpUtils {

    public static int randomInteger(int bound) {
        return new Random().nextInt(bound);
    }

}
