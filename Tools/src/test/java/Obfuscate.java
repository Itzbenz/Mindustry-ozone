import Atom.Meth;
import Atom.Random;
import Atom.Time.Countdown;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.google.googlejavaformat.java.FormatterException;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

//Personal use
public class Obfuscate {
    public static double avg(ArrayList<Long> arr) {
        double sum = 0;
        for (long l : arr) {
            sum += l;
        }
        return sum / arr.size();
    }

    public static void main(String[] args) throws Exception {
        File root = new File("src/main/java/Ozone/");
        File desktop = new File("Desktop/src/main/java/Ozone/");
        File android = new File("Android/src/main/java/Ozone/");
        if (!root.exists()) throw new RuntimeException(root.getAbsolutePath() + " doesnt exists");
        if (!desktop.exists()) throw new RuntimeException(desktop.getAbsolutePath() + " doesnt exists");
        if (!android.exists()) throw new RuntimeException(android.getAbsolutePath() + " doesnt exists");

        ArrayList<File> core = recurse(root);
        ArrayList<File> desk = recurse(desktop);
        ArrayList<File> droid = recurse(android);
        System.out.println("Total: " + (core.size() + desk.size() + droid.size()));
        System.out.println("Commit git crime");
        Process p = Runtime.getRuntime().exec("git commit -m \"yeet on earth, the compiler mean death\"");
        obfuscate(core);
        obfuscate(desk);
        obfuscate(droid);
    }

    public static void obfuscate(List<File> files) throws IOException, FormatterException {
        "dont //remove this idiot".length();//"does it"//
        for (File f : files) {
            String extension = "";
            int i = f.getName().lastIndexOf('.');
            if (i > 0) {
                extension = f.getName().substring(i + 1);
            }
            if (!extension.equals("java")) continue;

            CompilationUnit compilationUnit = StaticJavaParser.parse(f);
            compilationUnit.findAll(StringLiteralExpr.class).forEach(node -> {
                if (!node.replace(StaticJavaParser.parseExpression(obfuscate(node.getValue()))))
                    throw new RuntimeException("Holy shit cant do shit: \n"
                            + "Obfuscated: " + obfuscate(node.getValue()) +
                            "\n" + "Original: " + node.getValue() + "" +
                            "\n" + "Detailed node: " + node.toString());
            });
            /*
            String removedComment = compilationUnit.toString();
            //List<String> formattedS = new ArrayList<>(Arrays.asList(new Formatter().formatSource(new String(Files.readAllBytes(f.toPath()))).split("\n")));
            List<String> formattedS = new ArrayList<>(Arrays.asList(removedComment.split("\n")));
            StringBuilder sb = new StringBuilder();
            for (String s : formattedS) {
                s = s.trim();
                if (s.isEmpty()) continue;
                if (s.startsWith("//")) continue;
                ArrayList<String> ar = yeet('"', s);
                if (ar.isEmpty()) {
                    sb.append(s).append("\n");
                    continue;
                }
                for (String sa : ar) {
                    String mod = (obfuscate(sa));
                    String org = ("\"" + sa + "\"");
                    s = replace(org, mod, s);
                    System.out.println(mod);
                    System.out.println(org);
                    System.out.println("\n");
                }
                sb.append(s);
            }
            String g = sb.toString();
            try {
                g = new Formatter().formatSource(sb.toString());
            } catch (Throwable t) {
                t.printStackTrace();
                System.out.println(sb.toString());
                System.out.println("ERRRRRRRRRRRRRRRRRR");
            }
            */
            System.out.println(f.getAbsolutePath());
            FileWriter f2 = new FileWriter(f, false);
            f2.write(compilationUnit.toString());
            f2.close();

        }

    }

    // public static s = "gabe//itch";
    // "oh//no";//"yet"
    public static boolean itIsSurrounded(char o, int current, String data) {
        ArrayList<Integer> openingIndex = new ArrayList<>();
        int i = 0;
        while (i != -1) {
            i = data.indexOf(o, i);
            if (i != -1)
                openingIndex.add(i);
        }
        boolean yes = false;
        for (int s : openingIndex) {
            if (s < current) yes = false;
            if (yes) {
                if (s > current) return true;
            } else {
                if (s < current) yes = true;
            }
        }
        return false;
    }

    public static String replace(String regex, String replace, String data) {
        return Pattern.compile(regex, Pattern.LITERAL).matcher(data).replaceFirst(replace);
    }

    // "pac" = new String(new byte[]{102144/912, 97 , 99})
    public static String obfuscate(String s) {

        String temp = "new String(new byte[]{";
        String teem = "})";
        StringBuilder sb = new StringBuilder();
        String startOffset = Random.getString(Random.getInt(s.length()));
        String endOffset = Random.getString(Random.getInt(s.length()));
        boolean shouldOffset = Random.getBool();
        if (s.isEmpty()) return temp + teem;
        sb.append(temp);

        if (shouldOffset && !startOffset.isEmpty()) {
            //start offset
            for (int c : startOffset.toCharArray()) {
                if (Random.getBool())
                    sb.append(c);
                else
                    sb.append("(byte)Math.round(Math.sqrt(").append(c * c).append("))");
                sb.append(',');
            }
        }
        //aa,
        //actual
        for (int c : s.toCharArray()) {
            if (Random.getBool())
                sb.append(c);
            else
                sb.append("(byte)Math.round(Math.sqrt(").append(c * c).append("))");
            sb.append(',');
        }
        //aa,bb,
        if (shouldOffset && !endOffset.isEmpty()) {
            //end offset
            for (int c : endOffset.toCharArray()) {
                if (Random.getBool())
                    sb.append(c);
                else
                    sb.append("(byte)Math.round(Math.sqrt(").append(c * c).append("))");
                sb.append(',');
            }
        }
        if (sb.charAt(sb.length() - 1) == ',')
            sb.deleteCharAt(sb.length() - 1);
        if (!shouldOffset)
            sb.append(teem);
        else {
            sb.append("}").append(", ").append(startOffset.length()).append(", ").append((startOffset.length() + s.length()) - endOffset.length()).append(")");
        }
        return sb.toString();

    }

    public static ArrayList<String> yeet(char s, String data) {

        ArrayList<String> dats = new ArrayList<>();
        if (!data.contains(String.valueOf(s))) return dats;
        try {
            Pattern pattern = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1");
            dats.addAll(Arrays.asList(pattern.split(data)));
            return dats;
        } catch (Throwable t) {
        }
        dats.clear();
        boolean f = false, skip = false;


        StringBuilder sb = new StringBuilder();
        for (char c : data.toCharArray()) {
            if (!skip)
                if (c == s) {
                    f = !f;
                    continue;
                }
            if (skip) skip = false;
            if (c == '\\') skip = true;
            if (f) sb.append(c);
            else {
                if (!sb.toString().replaceAll(String.valueOf(s), "").isEmpty()) {
                    dats.add(sb.toString());
                }
                sb = new StringBuilder();
            }
        }
        return dats;
    }

    public static ArrayList<File> recurse(File f) {
        ArrayList<File> files = new ArrayList<>();
        for (File c : Objects.requireNonNull(f.listFiles())) {
            if (c.isDirectory())
                files.addAll(recurse(c));
            else
                files.add(c);
        }
        return files;
    }

    @Test
    public void name() {
        ArrayList<Long> obfuscated = new ArrayList<>();
        ArrayList<Long> normal = new ArrayList<>();
        ArrayList<String> yet = new ArrayList<>();
        yet.add("//yes yes no //yes yes no");
        yet.add("String s = \"literal\";//ofc");
        yet.add("String gay = \"absolute\";");
        for (int i = 0; i < 1000; i++) {
            Countdown.start();
            String s = new String(new byte[]{984928 / 8794, 852339 / 8787, 447579 / 4521, 768902 / 7186, 555228 / 5724, 795469 / 7723, 294920 / 2920, 318112 / 9941, 416488 / 5272, 87352 / 716, 677100 / 6100, 944350 / 8585, 399354 / 3954, 11730 / 255, 43148 / 644, 291153 / 2623, 986777 / 9053, 929225 / 8525, 556586 / 5738, 347490 / 3159, 820900 / 8209, 669185 / 5819, 240779 / 4081,});
            obfuscated.add(Countdown.stop());
            System.out.println(Countdown.result());
            System.out.println("vs");
            Countdown.start();
            String lol = new String("package Ozone.Commands;");
            normal.add(Countdown.stop());
            System.out.println(Countdown.result());
        }
        System.out.println("obfuscated avg, max, min: " + avg(obfuscated) + ", " + Meth.max(obfuscated.toArray(new Long[0])) + ", " + Meth.min(obfuscated.toArray(new Long[0])));
        System.out.println("normal avg, max, min: " + avg(normal) + ", " + Meth.max(normal.toArray(new Long[0])) + ", " + Meth.min(normal.toArray(new Long[0])));
    }
}
