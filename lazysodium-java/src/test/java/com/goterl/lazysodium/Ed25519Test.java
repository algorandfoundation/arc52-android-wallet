package com.goterl.lazysodium;

import com.goterl.lazysodium.exceptions.SodiumException;
import com.goterl.lazysodium.interfaces.Ed25519;
import com.goterl.lazysodium.interfaces.Ed25519.Ed25519Point;
import com.goterl.lazysodium.utils.Base64MessageEncoder;
import com.goterl.lazysodium.utils.HexMessageEncoder;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

// get test vectors here: https://ed25519.cr.yp.to/software.html

public class Ed25519Test extends BaseTest {

    @Test
    public void scalarConversions() {
        BigInteger n = BigInteger.valueOf(1);
        BigInteger m = Ed25519.bytesToScalar(Ed25519.scalarToBytes(n));
        assertEquals(n, m);

        m = Ed25519.bytesToScalar(Ed25519.scalarToBytes(n, false));
        assertEquals(n, m);

        n = BigInteger.valueOf(12345678);
        m = Ed25519.bytesToScalar(Ed25519.scalarToBytes(n));
        assertEquals(n, m);

        m = Ed25519.bytesToScalar(Ed25519.scalarToBytes(n, false));
        assertEquals(n, m);
    }

    @Test
    public void randomPoint() {
        // This should not throw
        Ed25519Point p = Ed25519Point.random(lazySodium);
        Ed25519Point q = Ed25519Point.random(lazySodium);

        // Random points should be non-equal
        assertNotEquals(p, q);
    }

    @Test
    public void invalidPoint() {
        // Test vectors originally from https://ristretto.group/test_vectors/ristretto255.html
        // Some points are commented out as they are deemed valid in Ed25519 but not Ristretto255
        // TODO: Replace with test vectors specifically for Ed25519

        String[] badEncodings = new String[] {
                // These are all bad because they're non-canonical field encodings.
                "00ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff",
                "ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff7f",
                "f3ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff7f",
                "edffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff7f",
                // These are all bad because they're negative field elements.
                "0100000000000000000000000000000000000000000000000000000000000000",
                "01ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff7f",
                "ed57ffd8c914fb201471d1c3d245ce3c746fcbe63a3679d51b6a516ebebe0e20",
                "c34c4e1826e5d403b78e246e88aa051c36ccf0aafebffe137d148a2bf9104562",
                "c940e5a4404157cfb1628b108db051a8d439e1a421394ec4ebccb9ec92a8ac78",
                "47cfc5497c53dc8e61c91d17fd626ffb1c49e2bca94eed052281b510b1117a24",
        //        "f1c6165d33367351b0da8f6e4511010c68174a03b6581212c71c0e1d026c3c72",
                 "87260f7a2f12495118360f02c26a470f450dadf34a413d21042b43b9d93e1309",
        //         // These are all bad because they give a nonsquare x^2.
                 "26948d35ca62e643e26a83177332e6b6afeb9d08e4268b650f1f5bbd8d81d371",
                "4eac077a713c57b4f4397629a4145982c661f48044dd3f96427d40b147d9742f",
                "de6a7b00deadc788eb6b6c8d20c0ae96c2f2019078fa604fee5b87d6e989ad7b",
                "bcab477be20861e01e4a0e295284146a510150d9817763caf1a6f4b422d67042",
        //        "2a292df7e32cababbd9de088d1d1abec9fc0440f637ed2fba145094dc14bea08",
                "f4a9e534fc0d216c44b218fa0c42d99635a0127ee2e53c712f70609649fdff22",
        //         "8268436f8c4126196cf64b3c7ddbda90746a378625f9813dd9b8457077256731",
                 "2810e5cbc2cc4d4eece54f61c6f69758e289aa7ab440b3cbeaa21995c2f4232b",
        //         // These are all bad because they give a negative xy value.
                 "3eb858e78f5a7254d8c9731174a94f76755fd3941c0ac93735c07ba14579630e",
        //         "a45fdc55c76448c049a1ab33f17023edfb2be3581e9c7aade8a6125215e04220",
                 "d483fe813c6ba647ebbfd3ec41adca1c6130c2beeee9d9bf065c8d151c5f396e",
                 "8a2e1d30050198c65a54483123960ccc38aef6848e1ec8f5f780e8523769ba32",
                 "32888462f8b486c68ad7dd9610be5192bbeaf3b443951ac1a8118419d9fa097b",
                 "227142501b9d4355ccba290404bde41575b037693cef1f438c47f8fbf35d1165",
                 "5c37cc491da847cfeb9281d407efc41e15144c876e0170b499a96a22ed31e01e",
                 "445425117cb8c90edcbc7c1cc0e74f747f2c1efa5630a967c64f287792a48a4b",
        //         // This is s = -1, which causes y = 0.
                 "ecffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff7f"
         };

        for (String badEncoding : badEncodings) {
            assertFalse(lazySodium.cryptoCoreEd25519IsValidPoint(badEncoding));

            try {
                // Calling Ed25519Point.fromHex with a bad encoding should throw
                Ed25519Point p = Ed25519Point.fromHex(lazySodium, badEncoding);
                fail(p.toHex());
            } catch (IllegalArgumentException e) {
                // expected
            }
        }

        // Wrong length
        byte[] invalidPoint = new byte[42];
        String invalidPointHex = "01";
        assertFalse(lazySodium.cryptoCoreEd25519IsValidPoint(invalidPoint));
        assertFalse(lazySodium.cryptoCoreEd25519IsValidPoint(invalidPointHex));

        try {
            // Calling Ed25519Point.fromBytes with a bad encoding should throw
            Ed25519Point p = Ed25519Point.fromBytes(lazySodium, invalidPoint);
            fail(p.toHex());
        } catch (IllegalArgumentException e) {
            // expected
        }

        try {
            // Calling Ed25519Point.fromHex with a bad encoding should throw
            Ed25519Point p = Ed25519Point.fromHex(lazySodium, invalidPointHex);
            fail(p.toHex());
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void fromUniform() throws Exception {
        byte[] hash = new byte[64];
        new Random().nextBytes(hash);

        Ed25519Point p = lazySodium.cryptoCoreEd25519FromUniform(hash);

        String hashHex = LazySodium.toHex(hash);
        Ed25519Point q = lazySodium.cryptoCoreEd25519FromUniform(hashHex);

        assertEquals(p, q);

        byte[] differentHash = new byte[64];
        new Random().nextBytes(differentHash);

        Ed25519Point different = lazySodium.cryptoCoreEd25519FromUniform(differentHash);
        assertNotEquals(p, different);

        byte[] invalidHash = new byte[42];
        try {
            lazySodium.cryptoCoreEd25519FromUniform(invalidHash);
            fail("Should throw");
        } catch (IllegalArgumentException e) {
            // expected
        }

        // TODO: Add test vectors corresponding to Elligator 2 Map to compare against
    }

    @Test
    public void scalarMultOne() throws Exception {
        // When multiplying with 1, the point should not change
        BigInteger one = BigInteger.ONE;

        // scalar encoding is little-endian
        String oneHex = "01" + IntStream.range(0, 31).mapToObj(i -> "00").collect(Collectors.joining());
        byte[] oneBytes = Ed25519.scalarBuffer();
        oneBytes[0] = 1;

        Ed25519Point point = lazySodium.cryptoCoreEd25519Random();
        Ed25519Point res1 = lazySodium.cryptoScalarMultEd25519Noclamp(one, point);
        Ed25519Point res2 = point.times(one);
        assertEquals(res1, res2);
        assertEquals(point, res1);

        res1 = lazySodium.cryptoScalarMultEd25519Noclamp(oneHex, point);
        assertEquals(point, res1);
        res1 = lazySodium.cryptoScalarMultEd25519Noclamp(oneBytes, point);
        assertEquals(point, res1);
    }

    @Test
    public void scalarMultZero() {
        // When multiplying with 0, it should throw
        BigInteger zero = BigInteger.ZERO;

        // scalar encoding is little-endian
        String zeroHex = IntStream.range(0, 32).mapToObj(i -> "00").collect(Collectors.joining());
        byte[] zeroBytes = Ed25519.scalarBuffer();
        Ed25519Point point = lazySodium.cryptoCoreEd25519Random();

        try {
            Ed25519Point res = lazySodium.cryptoScalarMultEd25519Noclamp(zero, point);
            fail(res.toHex());
        } catch (SodiumException e) {
            // expected
        }

        try {
            Ed25519Point res = point.times(zero);
            fail(res.toHex());
        } catch (SodiumException e) {
            // expected
        }

        try {
            Ed25519Point res = lazySodium.cryptoScalarMultEd25519Noclamp(zeroHex, point);
            fail(res.toHex());
        } catch (SodiumException e) {
            // expected
        }
        try {
            Ed25519Point res = lazySodium.cryptoScalarMultEd25519Noclamp(zeroBytes, point);
            fail(res.toHex());
        } catch (SodiumException e) {
            // expected
        }
    }

    @Test
    public void scalarMultBase() throws Exception {
        // Test vectors for ed25519
        String[] expected = new String[] {
                // This is the basepoint
                "5866666666666666666666666666666666666666666666666666666666666666",
                // These are small multiples of the basepoint
                "C9A3F86AAE465F0E56513864510F3997561FA2C9E85EA21DC2292309F3CD6022",
                "D4B4F5784868C3020403246717EC169FF79E26608EA126A1AB69EE77D1B16712",
                "2F1132CA61AB38DFF00F2FEA3228F24C6C71D58085B80E47E19515CB27E8D047",
                "EDC876D6831FD2105D0B4389CA2E283166469289146E2CE06FAEFE98B22548DF",
                "F47E49F9D07AD2C1606B4D94067C41F9777D4FFDA709B71DA1D88628FCE34D85",
                "B862409FB5C4C4123DF2ABF7462B88F041AD36DD6864CE872FD5472BE363C5B1",
                "B4B937FCA95B2F1E93E41E62FC3C78818FF38A66096FAD6E7973E5C90006D321",
                "C0F1225584444EC730446E231390781FFDD2F256E9FCBEB2F40DDDC2C2233D7F",
                "2C7BE86AB07488BA43E8E03D85A67625CFBF98C8544DE4C877241B7AAAFC7FE3",
                "1337036AC32D8F30D4589C3C1C595812CE0FFF40E37C6F5A97AB213F318290AD",
                "F9E42D2EDC81D23367967352B47E4856B82578634E6C1DE72280CE8B60CE70C0",
                "801F40EAAEE1EF8723279A28B2CF4037B889DAD222604678748B53ED0DB0DB92",
                "39289C8998FD69835C26B619E89848A7BF02B7CB7AD1BA1581CBC4506F2550CE",
                "DF5C2EADC44C6D94A19A9AA118AFE5AC3193D26401F76251F522FF042DFBCB92",
        };

        Ed25519Point base = Ed25519Point.base(lazySodium);
        assertEquals(Ed25519Point.fromHex(lazySodium, expected[0]), base);

        for (byte i = 1; i <= 15; ++i) {
            BigInteger n = BigInteger.valueOf(i);

            // scalar encoding is little-endian
            String nHex = String.format("%02x", i) + IntStream.range(0, 31).mapToObj(j -> "00")
                    .collect(Collectors.joining());
            byte[] nBytes = Ed25519.scalarBuffer();
            nBytes[0] = i;

            Ed25519Point res1 = lazySodium.cryptoScalarMultEd25519BaseNoclamp(n);
            Ed25519Point res2 = lazySodium.cryptoScalarMultEd25519BaseNoclamp(nHex);
            Ed25519Point res3 = lazySodium.cryptoScalarMultEd25519BaseNoclamp(nBytes);
            Ed25519Point res4 = base.times(n);

            Ed25519Point expectedPoint = Ed25519Point.fromHex(lazySodium, expected[i - 1]);

            assertEquals(expectedPoint, res1);
            assertEquals(res1, res2);
            assertEquals(res2, res3);
            assertEquals(res3, res4);
        }
    }

    @Test
    public void addSub() throws Exception {
        //final Ed25519Point ZERO = Ed25519Point.zero(lazySodium);
        final Ed25519Point BASE = Ed25519Point.base(lazySodium);
        
        Ed25519Point p = Ed25519Point.random(lazySodium);
        Ed25519Point sum = p.plus(BASE); // Add BASE to p
        Ed25519Point diff = sum.minus(BASE); // Subtract BASE from the sum
        
        // Check that adding BASE to p and then subtracting BASE gives back p
        assertEquals(p, diff);
        
        // Check that subtracting p from the sum gives back BASE
        assertEquals(BASE, sum.minus(p));
        
        // Check that adding p to BASE and then subtracting p gives back BASE
        assertEquals(BASE, sum.minus(p));
        
    }

    @Test
    public void randomScalar() {
        BigInteger s1 = lazySodium.cryptoCoreEd25519ScalarRandom();
        BigInteger s2 = lazySodium.cryptoCoreEd25519ScalarRandom();
        BigInteger s3 = lazySodium.cryptoCoreEd25519ScalarRandom();

        // all three scalars should be positive and non-equal
        assertFalse(s1.compareTo(BigInteger.ZERO) < 0);
        assertFalse(s2.compareTo(BigInteger.ZERO) < 0);
        assertFalse(s3.compareTo(BigInteger.ZERO) < 0);

        assertNotEquals(s1, s2);
        assertNotEquals(s1, s3);
        assertNotEquals(s2, s3);
    }

    @Test
    public void reduceScalar() {
        // Those should be reduced to exactly x - L
        BigInteger oneModL = Ed25519.ED25519_L.add(BigInteger.ONE);
        BigInteger tenModL = Ed25519.ED25519_L.add(BigInteger.TEN);

        BigInteger rand = lazySodium.cryptoCoreEd25519ScalarRandom();
        BigInteger randModL = Ed25519.ED25519_L.add(rand);

        // Those should be reduced to 0
        BigInteger lTimes2 = Ed25519.ED25519_L.multiply(BigInteger.valueOf(2));
        BigInteger lTimes42 = Ed25519.ED25519_L.multiply(BigInteger.valueOf(42));

        assertEquals(BigInteger.ONE, lazySodium.cryptoCoreEd25519ScalarReduce(oneModL));
        assertEquals(BigInteger.TEN, lazySodium.cryptoCoreEd25519ScalarReduce(tenModL));
        assertEquals(rand, lazySodium.cryptoCoreEd25519ScalarReduce(randModL));

        assertEquals(BigInteger.ZERO, lazySodium.cryptoCoreEd25519ScalarReduce(lTimes2));
        assertEquals(BigInteger.ZERO, lazySodium.cryptoCoreEd25519ScalarReduce(lTimes42));

        // Scalars within [0, L[ should not be reduced
        assertEquals(BigInteger.ZERO,
                lazySodium.cryptoCoreEd25519ScalarReduce(BigInteger.ZERO));
        assertEquals(BigInteger.ONE, lazySodium.cryptoCoreEd25519ScalarReduce(BigInteger.ONE));
        assertEquals(BigInteger.TEN, lazySodium.cryptoCoreEd25519ScalarReduce(BigInteger.TEN));
        assertEquals(rand, lazySodium.cryptoCoreEd25519ScalarReduce(rand));

        // Invalid scalar hex strings should not be accepted
        // too short
        String invalidHex = "01";
        try {
            BigInteger s = lazySodium.cryptoCoreEd25519ScalarReduce(invalidHex);
            fail(s.toString());
        } catch (IllegalArgumentException e) {
            // expected
        }

        // little-endian hex strings should be accepted
        String validHex = "01" + IntStream.range(0, 63).mapToObj(i -> "00")
                .collect(Collectors.joining());
        assertEquals(BigInteger.ONE, lazySodium.cryptoCoreEd25519ScalarReduce(validHex));
    }

    @Test
    public void scalarInvert() throws Exception {
        for (int i = 0; i < 50; ++i) {
            BigInteger s = lazySodium.cryptoCoreEd25519ScalarRandom();
            byte[] sBytes = Ed25519.scalarToBytes(s);
            String sHex = lazySodium.toHexStr(sBytes);

            BigInteger sInv = lazySodium.cryptoCoreEd25519ScalarInvert(s);
            BigInteger sInvBytes = lazySodium.cryptoCoreEd25519ScalarInvert(sBytes);
            BigInteger sInvHex = lazySodium.cryptoCoreEd25519ScalarInvert(sHex);

            assertEquals(BigInteger.ONE, s.multiply(sInv).mod(Ed25519.ED25519_L));
            assertEquals(sInv, sInvBytes);
            assertEquals(sInv, sInvHex);
        }

        // Invalid scalar hex strings should not be accepted
        // too short
        String invalidHex = "01";
        try {
            BigInteger s = lazySodium.cryptoCoreEd25519ScalarInvert(invalidHex);
            fail(s.toString());
        } catch (IllegalArgumentException e) {
            // expected
        }

        // 0 can't be inverted
        try {
            BigInteger i = lazySodium.cryptoCoreEd25519ScalarInvert(BigInteger.ZERO);
            fail(i.toString());
        } catch (SodiumException e) {
            // expected
        }
    }

    @Test
    public void scalarNegate() {
        for (int i = 0; i < 50; ++i) {
            BigInteger s = lazySodium.cryptoCoreEd25519ScalarRandom();
            byte[] sBytes = Ed25519.scalarToBytes(s);
            String sHex = lazySodium.toHexStr(sBytes);

            BigInteger sNeg = lazySodium.cryptoCoreEd25519ScalarNegate(s);
            BigInteger sNegBytes = lazySodium.cryptoCoreEd25519ScalarNegate(sBytes);
            BigInteger sNegHex = lazySodium.cryptoCoreEd25519ScalarNegate(sHex);

            assertEquals(BigInteger.ZERO, s.add(sNeg).mod(Ed25519.ED25519_L));
            assertEquals(sNeg, sNegBytes);
            assertEquals(sNeg, sNegHex);
        }

        // Invalid scalar hex strings should not be accepted
        // too short
        String invalidHex = "01";
        try {
            BigInteger s = lazySodium.cryptoCoreEd25519ScalarNegate(invalidHex);
            fail(s.toString());
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void scalarComplement() {
        for (int i = 0; i < 50; ++i) {
            BigInteger s = lazySodium.cryptoCoreEd25519ScalarRandom();
            byte[] sBytes = Ed25519.scalarToBytes(s);
            String sHex = lazySodium.toHexStr(sBytes);

            BigInteger sComp = lazySodium.cryptoCoreEd25519ScalarComplement(s);
            BigInteger sCompBytes = lazySodium.cryptoCoreEd25519ScalarComplement(sBytes);
            BigInteger sCompHex = lazySodium.cryptoCoreEd25519ScalarComplement(sHex);

            assertEquals(BigInteger.ONE, s.add(sComp).mod(Ed25519.ED25519_L));
            assertEquals(sComp, sCompBytes);
            assertEquals(sComp, sCompHex);
        }

        // Invalid scalar hex strings should not be accepted
        // too short
        String invalidHex = "01";
        try {
            BigInteger s = lazySodium.cryptoCoreEd25519ScalarNegate(invalidHex);
            fail(s.toString());
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void scalarAdd() {
        for (int i = 0; i < 50; ++i) {
            BigInteger s1 = lazySodium.cryptoCoreEd25519ScalarRandom();
            BigInteger s2 = lazySodium.cryptoCoreEd25519ScalarRandom();

            BigInteger expected = s1.add(s2).mod(Ed25519.ED25519_L);

            byte[] s1Bytes = Ed25519.scalarToBytes(s1);
            byte[] s2Bytes = Ed25519.scalarToBytes(s2);

            String s1Hex = lazySodium.toHexStr(s1Bytes);
            String s2Hex = lazySodium.toHexStr(s2Bytes);

            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarAdd(s1, s2));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarAdd(s1Bytes, s2));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarAdd(s1, s2Bytes));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarAdd(s1Hex, s2));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarAdd(s1, s2Hex));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarAdd(s1Bytes, s2Hex));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarAdd(s1Hex, s2Bytes));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarAdd(s1Hex, s2Hex));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarAdd(s1Bytes, s2Bytes));
        }

        // Invalid scalar hex strings should not be accepted
        // too short
        String invalidHex = "01";
        try {
            BigInteger s = lazySodium.cryptoCoreEd25519ScalarAdd(invalidHex, BigInteger.ONE);
            fail(s.toString());
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            BigInteger s = lazySodium.cryptoCoreEd25519ScalarAdd(BigInteger.ONE, invalidHex);
            fail(s.toString());
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void scalarSub() {
        for (int i = 0; i < 50; ++i) {
            BigInteger s1 = lazySodium.cryptoCoreEd25519ScalarRandom();
            BigInteger s2 = lazySodium.cryptoCoreEd25519ScalarRandom();

            BigInteger expected = s1.subtract(s2).mod(Ed25519.ED25519_L);

            byte[] s1Bytes = Ed25519.scalarToBytes(s1);
            byte[] s2Bytes = Ed25519.scalarToBytes(s2);

            String s1Hex = lazySodium.toHexStr(s1Bytes);
            String s2Hex = lazySodium.toHexStr(s2Bytes);

            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarSub(s1, s2));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarSub(s1Bytes, s2));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarSub(s1, s2Bytes));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarSub(s1Hex, s2));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarSub(s1, s2Hex));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarSub(s1Bytes, s2Hex));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarSub(s1Hex, s2Bytes));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarSub(s1Hex, s2Hex));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarSub(s1Bytes, s2Bytes));
        }

        // Invalid scalar hex strings should not be accepted
        // too short
        String invalidHex = "01";
        try {
            BigInteger s = lazySodium.cryptoCoreEd25519ScalarSub(invalidHex, BigInteger.ONE);
            fail(s.toString());
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            BigInteger s = lazySodium.cryptoCoreEd25519ScalarSub(BigInteger.ONE, invalidHex);
            fail(s.toString());
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void scalarMul() {
        for (int i = 0; i < 50; ++i) {
            BigInteger s1 = lazySodium.cryptoCoreEd25519ScalarRandom();
            BigInteger s2 = lazySodium.cryptoCoreEd25519ScalarRandom();

            BigInteger expected = s1.multiply(s2).mod(Ed25519.ED25519_L);

            byte[] s1Bytes = Ed25519.scalarToBytes(s1);
            byte[] s2Bytes = Ed25519.scalarToBytes(s2);

            String s1Hex = lazySodium.toHexStr(s1Bytes);
            String s2Hex = lazySodium.toHexStr(s2Bytes);

            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarMul(s1, s2));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarMul(s1Bytes, s2));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarMul(s1, s2Bytes));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarMul(s1Hex, s2));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarMul(s1, s2Hex));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarMul(s1Bytes, s2Hex));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarMul(s1Hex, s2Bytes));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarMul(s1Hex, s2Hex));
            assertEquals(expected, lazySodium.cryptoCoreEd25519ScalarMul(s1Bytes, s2Bytes));
        }

        // Invalid scalar hex strings should not be accepted
        // too short
        String invalidHex = "01";
        try {
            BigInteger s = lazySodium.cryptoCoreEd25519ScalarMul(invalidHex, BigInteger.ONE);
            fail(s.toString());
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            BigInteger s = lazySodium.cryptoCoreEd25519ScalarMul(BigInteger.ONE, invalidHex);
            fail(s.toString());
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test
    public void encoders() {
        LazySodiumJava lsHex = new LazySodiumJava(new SodiumJava(), new HexMessageEncoder());
        LazySodiumJava lsBase64 = new LazySodiumJava(new SodiumJava(), new Base64MessageEncoder());

        Ed25519Point randomPoint = Ed25519Point.random(lazySodium);

        Ed25519Point hexPoint = Ed25519Point.fromBytes(lsHex, randomPoint.toBytes());
        Ed25519Point base64Point = Ed25519Point.fromBytes(lsBase64, randomPoint.toBytes());

        String hexEncoded = hexPoint.encode();
        String base64Encoded = base64Point.encode();

        assertEquals(randomPoint.toHex(), hexEncoded);
        assertEquals(Base64.getEncoder().encodeToString(randomPoint.toBytes()), base64Encoded);
    }
}
