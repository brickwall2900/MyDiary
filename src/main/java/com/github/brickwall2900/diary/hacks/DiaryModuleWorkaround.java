package com.github.brickwall2900.diary.hacks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DiaryModuleWorkaround {
    public static void main(String[] args) {
        System.err.println("Workaround for module java.desktop does not \"opens javax.swing.text.html.parser\" to module diary.main" /* or unnamed module or whatever the hell */);
        System.err.println("Code is not coding!!");

        // give all the internals TvT
        List<String> crap = buildCrap();
        List<String> newArgs = JavaProcessHelper.createArguments(crap);
        try {
            JavaProcessHelper.relaunch(newArgs).waitFor();
        } catch (IOException e) {
            throw new IllegalStateException("naurr");
        } catch (InterruptedException e) {
// do something with the exception...
        }
    }

    private static List<String> buildCrap() {
        List<String> crap = new ArrayList<>();
        crap.add("--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED");
        crap.add("--add-opens=java.desktop/java.awt=ALL-UNNAMED");
        crap.add("--add-opens=java.desktop/sun.awt.windows=ALL-UNNAMED");
        crap.add("--add-opens=java.desktop/javax.swing.plaf.basic=ALL-UNNAMED");
        crap.add("--add-opens=java.desktop/javax.swing.text.html.parser=ALL-UNNAMED");
        crap.add("--add-opens=java.desktop/sun.awt=ALL-UNNAMED");
        crap.add("-Ddiary.moduleAccessWorkaround=true");
        return crap;
    }
}
