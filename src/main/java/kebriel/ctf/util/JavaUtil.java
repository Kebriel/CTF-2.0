package kebriel.ctf.util;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Utility class that I wrote to simplify/add ease to certain
 * functions that I found myself needing while writing this plugin.
 *
 * This class is differentiated from MinecraftUtils as none of the
 * methods here pertain to Spigot/NMS or Minecraft broadly at all --
 * they mostly concern utilities relating to Strings, math functions,
 * collections, etc.
 */

public class JavaUtil {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");
    private static final TreeMap<Integer, String> ROMAN_NUMERALS = new TreeMap<>();

    static {
        ROMAN_NUMERALS.put(1000, "M");
        ROMAN_NUMERALS.put(900, "CM");
        ROMAN_NUMERALS.put(500, "D");
        ROMAN_NUMERALS.put(400, "CD");
        ROMAN_NUMERALS.put(100, "C");
        ROMAN_NUMERALS.put(90, "XC");
        ROMAN_NUMERALS.put(50, "L");
        ROMAN_NUMERALS.put(40, "XL");
        ROMAN_NUMERALS.put(10, "X");
        ROMAN_NUMERALS.put(9, "IX");
        ROMAN_NUMERALS.put(5, "V");
        ROMAN_NUMERALS.put(4, "IV");
        ROMAN_NUMERALS.put(1, "I");
    }

    public static <T> Class<T> getRawType(T obj) {
        return switch (obj.getClass().getName()) {
            case "java.lang.Byte" -> (Class<T>) byte.class;
            case "java.lang.Short" -> (Class<T>) short.class;
            case "java.lang.Integer" -> (Class<T>) int.class;
            case "java.lang.Long" -> (Class<T>) long.class;
            case "java.lang.Float" -> (Class<T>) float.class;
            case "java.lang.Double" -> (Class<T>) double.class;
            case "java.lang.Boolean" -> (Class<T>) boolean.class;
            case "java.lang.Character" -> (Class<T>) char.class;
            default -> (Class<T>) obj.getClass();
        };
    }

    public static boolean isNumeric(Object val) {
        if(val instanceof CharSequence c) {
            return NUMBER_PATTERN.matcher(c).matches();
        }
        return val instanceof Number;
    }

    public static boolean isTypeNumeric(Class<?> type) {
        return Number.class.isAssignableFrom(type);
    }

    public static <T extends Number> T performAmbiguousMath(T value, T operand, char operator) {
        switch(operator) {
            case '+':
            case '-':
            case '*':
            case '/':
                break;
            default:
                throw new IllegalArgumentException("The symbol '" + operator + "' is not a valid mathematical operator");
        }

        if(value instanceof Integer && operand instanceof Integer) {
            int val = (Integer) value;
            int oper = (Integer) operand;

            return switch (operator) {
                case '+' -> (T) Integer.valueOf(val + oper);
                case '-' -> (T) Integer.valueOf(val - oper);
                case '*' -> (T) Integer.valueOf(val * oper);
                case '/' -> (T) Integer.valueOf(val / oper);
                default -> null;
            };
        }
        if(value instanceof Double && operand instanceof Double) {
            double val = (Double) value;
            double oper = (Double) operand;

            return switch (operator) {
                case '+' -> (T) Double.valueOf(val + oper);
                case '-' -> (T) Double.valueOf(val - oper);
                case '*' -> (T) Double.valueOf(val * oper);
                case '/' -> (T) Double.valueOf(val / oper);
                default -> null;
            };
        }
        if(value instanceof Float && operand instanceof Float) {
            float val = (Float) value;
            float oper = (Float) operand;

            return switch (operator) {
                case '+' -> (T) Float.valueOf(val + oper);
                case '-' -> (T) Float.valueOf(val - oper);
                case '*' -> (T) Float.valueOf(val * oper);
                case '/' -> (T) Float.valueOf(val / oper);
                default -> null;
            };
        }
        if(value instanceof Long && operand instanceof Long) {
            long val = (Long) value;
            long oper = (Long) operand;

            return switch (operator) {
                case '+' -> (T) Long.valueOf(val + oper);
                case '-' -> (T) Long.valueOf(val - oper);
                case '*' -> (T) Long.valueOf(val * oper);
                case '/' -> (T) Long.valueOf(val / oper);
                default -> null;
            };
        }

        // Returns before getting here unless it's a wrong number type
        throw new IllegalArgumentException("Unsupported number type passed!");
    }

    public static boolean arePreciseEqual(Location loc1, Location loc2, double epsilon) {
        if (loc1 == null || loc2 == null) {
            return false;
        }

        if (!loc1.getWorld().equals(loc2.getWorld())) {
            return false;
        }

        if (Math.abs(loc1.getX() - loc2.getX()) > epsilon) {
            return false;
        }

        if (Math.abs(loc1.getY() - loc2.getY()) > epsilon) {
            return false;
        }

        if (Math.abs(loc1.getZ() - loc2.getZ()) > epsilon) {
            return false;
        }

        if (Math.abs(loc1.getPitch() - loc2.getPitch()) > epsilon) {
            return false;
        }

        return !(Math.abs(loc1.getYaw() - loc2.getYaw()) > epsilon);
    }

    public static String stringReplaceAll(String orig, String find, Object replaceWith) {
        StringBuilder builder = new StringBuilder(orig);
        while(builder.indexOf(find) != -1) {
            int index = builder.indexOf(find);
            builder.replace(index, index + find.length(), String.valueOf(replaceWith));
        }
        return builder.toString();
    }

    public static StringBuilder builderReplaceAll(StringBuilder builder, String find, Object replaceWith) {
        while(builder.indexOf(find) != -1) {
            int index = builder.indexOf(find);
            builder.replace(index, index + find.length(), String.valueOf(replaceWith));
        }
        return builder;
    }

    public static boolean builderReplaceFirst(StringBuilder builder, String find, Object replaceWith) {
        boolean success = false;
        if(builder.indexOf(find) != -1) {
            int index = builder.indexOf(find);
            builder.replace(index, index + find.length(), String.valueOf(replaceWith));
            success = true;
        }
        return success;
    }

    public static boolean builderContains(StringBuilder builder, String find) {
        return builder.indexOf(find) != -1;
    }

    public static boolean stringContains(String str, String find) {
        return new StringBuilder(str).indexOf(find) != -1;
    }

    public static String stringMultiplyValue(String orig, String multiply, int times, boolean comma) {
        StringBuilder builder = new StringBuilder(orig);
        int index = builder.indexOf(multiply);
        for(int i = 0; i < times; i++) {
            if(index != -1) {
                index+=multiply.length();
                multiply = " " + multiply;
                if(comma)
                    multiply = "," + multiply;
                builder.insert(index, multiply);
            }
        }
        return builder.toString();
    }

    public static byte[] convertUUIDToBytes(UUID id) {
        byte[] IDBytes = new byte[16];

        ByteBuffer.wrap(IDBytes)
                .putLong(id.getMostSignificantBits())
                .putLong(id.getLeastSignificantBits());
        return IDBytes;
    }

    public static UUID convertBytesToUUID(byte[] raw) {
        return new UUID(
                (ByteBuffer.wrap(raw, 0, 8).getLong()),
                (ByteBuffer.wrap(raw, 8, 8).getLong())
        );
    }

    public static void safeLatchWait(CountDownLatch latch) {
        try {
            latch.await();
        } catch(InterruptedException ex) {
            throw new RuntimeException("A waiting function was interrupted");
        }
    }

    /**
     * Derives an angle value between -180 and 180 from any sized
     * number
     * @param angle the raw angle value
     */
    public static float wrapAngle(float angle) {
        angle %= 360;
        if(angle > 180) {
            angle -= 360;
        }else if(angle < -180) {
            angle += 360;
        }
        return angle;
    }

    /**
     * Converts an angle into a usable byte format
     * for network efficiency purposes re: usage of
     * Minecraft's protocol
     * @param angle a formatted float angle, between -180 and 180
     */
    public static byte encodeAngle(float angle) {
        if(angle < -180 || angle > 180)
            throw new IllegalArgumentException("Must be an angle between -180 and 180");
        return (byte) ((angle * 256.0F) / 360.0F);
    }

    public static <T> Collection<T> collectionDifferenceOfCollections(Collection<T> a, Collection<T> b) {
        Set<T> difference = new HashSet<>(b);
        difference.removeAll(a);
        return difference;
    }

    public static <E> Set<E> addToSet(Set<E> set, E element) {
        set.add(element);
        return set;
    }

    public static <T> T[] typeArray(Collection<T> collection, Class<T> clazz) {
        @SuppressWarnings("unchecked")
        T[] array = (T[]) Array.newInstance(clazz, collection.size());
        return collection.toArray(array);
    }

    public static boolean withinByteRange(double... values) {
        for(double value : values) {
            double scaledValue = value * 32.0;
            if(!(scaledValue >= -128.0 && scaledValue <= 127.0)) return false;
        }
        return true;
    }

    public static String capitalizeFirstLetter(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    public static int occurrencesInString(String search, String sequence) {
        if(search == null || sequence == null || sequence.length() == 0)
            return 0;

        int count = 0;
        int index = 0;

        while((index = search.indexOf(sequence, index)) != -1) {
            count++;
            index += sequence.length();
        }

        return count;
    }

    public static <K, V> K getKeyByValue(Map<K, V> map, V value) {
        for(Map.Entry<K, V> entry : map.entrySet())
            if(entry.getValue().equals(value))
                return entry.getKey();
        return null;
    }

    public static boolean areIntElementsEqual(Collection<Integer> nums) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;

        for(int number : nums) {
            min = Math.min(min, number);
            max = Math.max(max, number);
        }

        return (max - min) == 0;
    }

    public static Vector rotateAroundY(Vector vec, double angle) {
        double rad = Math.toRadians(angle);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);
        return new Vector(vec.getX() * cos + vec.getZ() * sin, vec.getY(), vec.getZ() * cos - vec.getX() * sin);
    }

    public static int countOccurrences(StringBuilder str, String target) {
        int occurrences = 0;
        int index = 0;
        while((index = str.indexOf(target, index)) != -1) {
            occurrences++;
            index += target.length();
        }
        return occurrences;
    }

    public static StringBuilder insertBefore(StringBuilder str, String before, String insert) {
        int index = 0;
        while((index = str.indexOf(before, index)) != -1) {
            str.insert(index, insert);
            index += insert.length() + before.length();
        }
        return str;
    }

    public static String asNumeral(int number) {
        if(number < 1 || number > 99999)
            throw new IllegalArgumentException("Provide a number 1-99999");
        int l =  ROMAN_NUMERALS.floorKey(number);
        if(number == l)
            return ROMAN_NUMERALS.get(number);
        return ROMAN_NUMERALS.get(l) + asNumeral(number-l);
    }

    public static UUID parseUUIDFromDashless(String id) {
        String uuidWithDashes =
                id.substring(0, 8) + "-" +
                        id.substring(8, 12) + "-" +
                        id.substring(12, 16) + "-" +
                        id.substring(16, 20) + "-" +
                        id.substring(20);
        return UUID.fromString(uuidWithDashes);
    }

    public static <T> List<T> exemptFromList(List<T> list, T... exempt) {
        List<T> result = new ArrayList<>(list);
        for(T ex : exempt) {
            if(list.contains(ex)) {
                result.remove(ex);
            }
        }
        return result;
    }

    public static <T> Set<T> exemptFromSet(Set<T> list, T... exempt) {
        Set<T> result = new HashSet<>(list);
        for(T ex : exempt)
            result.remove(ex);
        return result;
    }

    public static void timedWait(int t, TimeUnit time) {
        MinecraftUtil.ensureAsync();

        long millis = time.toMillis(t);
        try {
            Thread.currentThread().wait(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static long ticksAsMillis(long ticks) {
        return ticks*50;
    }

    public static <T> T lazyGet(Future<T> future) {
        try {
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static double percentageToFraction(int percentage) {
        return (double)percentage/100;
    }

    public static double percentageToFractionInverted(int percentage) {
        return 1-((double)percentage/100);
    }
}
