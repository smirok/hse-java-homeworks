package ru.hse.java.function;

import org.junit.jupiter.api.*;

import java.io.*;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class FunctionTest {
    private static final int DATA_SIZE = 1000;

    @Test
    public void testFunction1Apply() {
        Function1<Integer, Integer> sqr = arg -> arg * arg;
        assertEquals(sqr.apply(5), 25);
        Function1<Integer, Integer> add3 = arg -> arg + 3;
        assertEquals(add3.apply(5), 8);
    }

    @Test
    public void testFunction1Compose() {
        Function1<Integer, Integer> sqr = arg -> arg * arg;
        Function1<Integer, Integer> add3 = arg -> arg + 3;
        assertEquals(sqr.compose(add3).apply(5), 28);
        assertEquals(add3.compose(sqr).apply(5), 64);
    }

    @Test
    @SuppressWarnings("unused")
    public void testFunction1ComposeCompiled() {
        Function1<Integer, Integer> add3 = arg -> arg + 3;
        Function1<Number, EOFException> funcToCompose = arg -> new EOFException();
        Function1<Integer, IOException> resultFunc = add3.compose(funcToCompose);
    }

    @Test
    public void testFunction1ComposeWithIdStress() {
        Function1<Integer, Integer> idCustom = arg -> arg;
        Function<Integer, Integer> mul3 = arg -> arg * 3;
        Function1<Integer, Integer> fCustom = idCustom.compose(arg -> arg * 3);
        Function<Integer, Integer> f = mul3.compose(Function.identity());

        Random random = new Random();
        for (int i = 0; i < DATA_SIZE; i++) {
            int num = random.nextInt(1_000_000);
            assertEquals(fCustom.apply(num), f.apply(num));
        }
    }

    /**
     * Function1.compose(Function1 g) : g ( f ( x ) )
     * Function.compose(Function g) : f ( g ( x ) )
     */
    @Test
    public void testFunction1ComposeStress() {
        Function1<Integer, Integer> mul3Custom = arg -> arg * 3;
        Function<Integer, Integer> substract4 = arg -> arg - 4;
        Function1<Integer, Integer> fCustom = mul3Custom.compose(arg -> arg - 4);
        Function<Integer, Integer> f = substract4.compose(arg -> arg * 3);

        Random random = new Random();
        for (int i = 0; i < DATA_SIZE; i++) {
            int num = random.nextInt(1_000_000);
            assertEquals(fCustom.apply(num), f.apply(num));
        }
    }

    @Test
    public void testFunction2Apply() {
        Function2<Integer, Character, Integer> intAddCharacter = Integer::sum;
        assertEquals(intAddCharacter.apply(1, 'a'), 98); // 'a' = 97
    }

    @Test
    public void testFunction2Compose() {
        Function1<Integer, Integer> mult2 = arg -> arg * 2;
        Function2<Integer, Character, Integer> intAddCharacter = Integer::sum;
        assertEquals(intAddCharacter.compose(mult2).apply(1, 'a'), 196); // 'a' = 97
    }

    @Test
    @SuppressWarnings("unused")
    public void testFunction2ComposeCompiled() {
        Function2<Integer, Integer, Integer> sum = Integer::sum;
        Function1<Number, EOFException> funcToCompose = arg -> new EOFException();
        Function2<Integer, Integer, IOException> resultFunc = sum.compose(funcToCompose);
    }

    /**
     * Function2.compose(Function1 g) : g ( f ( x , y) )
     * BiFunction.andThen(BiFunction g) : g ( f ( x , y ) )
     */
    @Test
    public void testFunction2ComposeStress() {
        Function2<Integer, Integer, Integer> Arg1mul3AddArg2Custom = (arg1, arg2) -> (arg1 * 3) + arg2;
        BiFunction<Integer, Integer, Integer> Arg1mul3AddArg2 = (arg1, arg2) -> (arg1 * 3) + arg2;
        Function2<Integer, Integer, Integer> fCustom = Arg1mul3AddArg2Custom.compose(arg -> arg / 2);
        BiFunction<Integer, Integer, Integer> f = Arg1mul3AddArg2.andThen(arg -> arg / 2);

        Random random = new Random();
        for (int i = 0; i < DATA_SIZE; i++) {
            int num1 = random.nextInt(1000);
            int num2 = random.nextInt(1000);
            assertEquals(fCustom.apply(num1, num2), f.apply(num1, num2));
        }
    }

    @Test
    public void testFunction2Bind1() {
        Function2<Integer, Character, Integer> intAddCharacter = Integer::sum;
        Function1<Character, Integer> charToIntPlusOne = arg -> arg + 1;
        assertEquals(intAddCharacter.bind1(1).apply('a'), charToIntPlusOne.apply('a'));
        assertNotEquals(intAddCharacter.bind1(1).apply('b'), charToIntPlusOne.apply('a'));
    }

    @Test
    public void testFunction2Bind2() {
        Function2<Integer, Character, Integer> intAddCharacter = Integer::sum;
        Function1<Integer, Integer> asciiAPlusInt = arg -> arg + 'a';
        assertEquals(intAddCharacter.bind2('a').apply(3), asciiAPlusInt.apply(3));
        assertNotEquals(intAddCharacter.bind2('a').apply(2), asciiAPlusInt.apply(3));
    }

    @Test
    public void testFunction2Curry() {
        Function2<Integer, Character, Integer> intAddCharacter = Integer::sum;
        Function1<Character, Integer> charToIntPlusOne = arg -> arg + 1;
        assertEquals(intAddCharacter.curry().apply(1).apply('a'), charToIntPlusOne.apply('a'));
        assertNotEquals(intAddCharacter.curry().apply(1).apply('b'), charToIntPlusOne.apply('a'));
    }

    @Test
    public void testPredicate() {
        Predicate<Integer> predicate = arg -> arg >= 0;
        assertFalse(predicate.apply(-1));
        assertTrue(predicate.apply(1337));
    }

    @Test
    public void testALWAYS_TRUE() {
        assertTrue(Predicate.ALWAYS_TRUE.apply("abc"));
    }

    @Test
    public void testALWAYS_FALSE() {
        assertFalse(Predicate.ALWAYS_FALSE.apply("abc"));
    }

    @Test
    public void testNotWithConstants() {
        assertFalse(Predicate.ALWAYS_TRUE.not().apply('a'));
        assertTrue(Predicate.ALWAYS_FALSE.not().apply('a'));
    }

    @Test
    public void testNot() {
        Predicate<Integer> predicate = arg -> arg >= 0;
        assertTrue(predicate.not().apply(-1));
        assertFalse(predicate.not().apply(1337));
    }

    @Test
    public void testAnd() {
        Predicate<Integer> firstPred = arg -> arg > 10;
        Predicate<Integer> secondPred = arg -> arg % 2 == 0;
        assertTrue(firstPred.and(secondPred).apply(12));
        assertFalse(secondPred.and(firstPred).apply(8));
    }

    @Test
    public void testAndWithAlways() {
        assertTrue(Predicate.ALWAYS_TRUE.and(Predicate.ALWAYS_TRUE).apply(12));
        assertFalse(Predicate.ALWAYS_FALSE.and(Predicate.ALWAYS_FALSE).apply(8));
    }

    @Test
    public void testAddLaziness() {
        AtomicBoolean EntriedToSecondPred = new AtomicBoolean(false);
        Predicate<Integer> firstPred = arg -> arg > 10;
        Predicate<Integer> secondPred = arg -> {
            EntriedToSecondPred.set(true);
            return arg % 2 == 0;
        };
        firstPred.and(secondPred).apply(8);
        assertFalse(EntriedToSecondPred.get());
    }

    @Test
    public void testOr() {
        Predicate<Integer> firstPred = arg -> arg > 10;
        Predicate<Integer> secondPred = arg -> arg % 2 == 0;
        assertTrue(firstPred.or(secondPred).apply(8));
        assertFalse(secondPred.or(firstPred).apply(9));
    }

    @Test
    public void testOrWithAlways() {
        assertTrue(Predicate.ALWAYS_TRUE.or(Predicate.ALWAYS_FALSE).apply(12));
        assertFalse(Predicate.ALWAYS_FALSE.or(Predicate.ALWAYS_FALSE).apply(8));
    }

    @Test
    public void testOrLaziness() {
        AtomicBoolean EntriedToSecondPred = new AtomicBoolean(false);
        Predicate<Integer> firstPred = arg -> arg > 10;
        Predicate<Integer> secondPred = arg -> {
            EntriedToSecondPred.set(true);
            return arg % 2 == 0;
        };
        firstPred.or(secondPred).apply(11);
        assertFalse(EntriedToSecondPred.get());
    }

    @Test
    public void testLongPredicate() {
        Predicate<Integer> firstPred = arg -> arg > 10;
        Predicate<Integer> secondPred = arg -> arg < 20;
        Predicate<Integer> thirdPred = arg -> arg % 2 == 0;
        Predicate<Integer> fourthPred = arg -> arg * arg > 200;
        assertTrue(firstPred.and(thirdPred).or(secondPred.and(fourthPred.not())).apply(13));
    }

    @Test
    public void testAddStress() {
        Random random = new Random();
        for (int i = 0; i < DATA_SIZE; i++) {
            int num = random.nextInt(1000);
            Predicate<Integer> firstPred = arg -> arg > num;
            java.util.function.Predicate<Integer> firstJavaPred = arg -> arg > num;

            int module = random.nextInt(10) + 1;
            Predicate<Integer> secondPred = arg -> arg % module == 0;
            java.util.function.Predicate<Integer> secondJavaPred = arg -> arg % module == 0;

            int arg = random.nextInt(1000);
            assertEquals(firstPred.and(secondPred).apply(arg), firstJavaPred.and(secondJavaPred).test(arg));
        }
    }

    @Test
    public void testOrStress() {
        Random random = new Random();
        for (int i = 0; i < DATA_SIZE; i++) {
            int num = random.nextInt(1000);
            Predicate<Integer> firstPred = arg -> arg > num;
            java.util.function.Predicate<Integer> firstJavaPred = arg -> arg > num;

            int module = random.nextInt(10) + 1;
            Predicate<Integer> secondPred = arg -> arg % module == 0;
            java.util.function.Predicate<Integer> secondJavaPred = arg -> arg % module == 0;

            int arg = random.nextInt(1000);
            assertEquals(firstPred.or(secondPred).apply(arg), firstJavaPred.or(secondJavaPred).test(arg));
        }
    }

    @Test
    public void testNotStress() {
        Random random = new Random();
        for (int i = 0; i < DATA_SIZE; i++) {
            int module = random.nextInt(10) + 1;
            Predicate<Integer> Pred = arg -> arg % module == 0;
            java.util.function.Predicate<Integer> javaPred = arg -> arg % module == 0;

            int arg = random.nextInt(1000);
            assertEquals(Pred.not().apply(arg), javaPred.negate().test(arg));
        }
    }
}
