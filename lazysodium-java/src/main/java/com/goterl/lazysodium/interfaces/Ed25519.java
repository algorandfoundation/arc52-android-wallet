package com.goterl.lazysodium.interfaces;

import com.goterl.lazysodium.LazySodium;
import com.goterl.lazysodium.exceptions.SodiumException;
import java.math.BigInteger;
import java.util.Arrays;

public interface Ed25519 {

    int ED25519_BYTES = 32;
    int ED25519_HASH_BYTES = 64;
    int ED25519_SCALAR_BYTES = 32;
    int ED25519_NON_REDUCED_SCALAR_BYTES = 64;
    BigInteger ED25519_L = BigInteger.valueOf(2).pow(252).add(
            new BigInteger("27742317777372353535851937790883648493", 10));

    static byte[] scalarToBytes(BigInteger n) {
        return scalarToBytes(n, true);
    }

    static byte[] scalarToBytes(BigInteger n, boolean reduced) {
        byte[] bigEndianBytes = n.toByteArray();
        int expectedCount = reduced ? ED25519_SCALAR_BYTES : ED25519_NON_REDUCED_SCALAR_BYTES;

        if (bigEndianBytes.length > expectedCount) {
            throw new IllegalArgumentException(
                    "The scalar value is too big to be represented in " + expectedCount + " bytes");
        }

        // Convert big-endian to little-endian
        byte[] littleEndianBytes = new byte[expectedCount];

        for (int i = 0; i < bigEndianBytes.length; ++i) {
            littleEndianBytes[i] = bigEndianBytes[bigEndianBytes.length - i - 1];
        }

        return littleEndianBytes;
    }

    static BigInteger bytesToScalar(byte[] bytes) {
        byte[] temp = new byte[bytes.length];

        // Convert little-endian to big-endian
        for (int i = 0; i < bytes.length; ++i) {
            temp[bytes.length - i - 1] = bytes[i];
        }

        return new BigInteger(temp);
    }

    static byte[] pointBuffer() {
        return new byte[ED25519_BYTES];
    }

    static byte[] scalarBuffer() {
        return new byte[ED25519_SCALAR_BYTES];
    }

    interface Native {

        /**
         * Returns whether the passed bytes represent a valid Ed25519 point.
         *
         * @param point the point to check, should be {@link Ed25519#ED25519_BYTES}
         *              bytes
         * @return true if valid
         */
        boolean cryptoCoreEd25519IsValidPoint(byte[] point);

        /**
         * Chooses a random Ed25519 point and puts its representation to {@code point}
         *
         * @param point the target array, must be {@link Ed25519#ED25519_BYTES} bytes
         */
        void cryptoCoreEd25519Random(byte[] point);

        /**
         * Maps a {@link Ed25519#ED25519_HASH_BYTES} bytes hash to a Ed25519 point
         * and puts its representation to {@code point}.
         *
         * @param point the target array, must be {@link Ed25519#ED25519_BYTES} bytes
         * @param hash  the hash, must be {@link Ed25519#ED25519_HASH_BYTES} bytes
         * @return true if successful
         */
        boolean cryptoCoreEd25519FromUniform(byte[] point, byte[] hash);

        /**
         * Multiplies the given Ed25519 {@code point} by the scalar {@code n} and puts
         * the
         * representation of the result into {@code result}.
         *
         * @param result the target array, must be {@link Ed25519#ED25519_BYTES} bytes
         * @param n      the scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES} bytes
         * @param point  the Ed25519 point, must be {@link Ed25519#ED25519_BYTES}
         *               bytes
         * @return true if successful
         */
        boolean cryptoScalarMultEd25519Noclamp(byte[] result, byte[] n, byte[] point);

        /**
         * Multiplies the Ed25519 base point by the scalar {@code n} and puts the
         * representation of the result into {@code result}.
         *
         * @param result the target array, must be {@link Ed25519#ED25519_BYTES} bytes
         * @param n      the scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES} bytes
         * @return true if successful
         */
        boolean cryptoScalarMultEd25519BaseNoclamp(byte[] result, byte[] n);

        /**
         * Adds two given Ed25519 points {@code p} and {@code q} and puts the
         * representation of
         * the result into {@code result}.
         *
         * @param result the target array, must be {@link Ed25519#ED25519_BYTES} bytes
         * @param p      the first Ed25519 point, must be {@link Ed25519#ED25519_BYTES}
         *               bytes
         * @param q      the second Ed25519 point, must be {@link Ed25519#ED25519_BYTES}
         *               bytes
         * @return true if successful
         */
        boolean cryptoCoreEd25519Add(byte[] result, byte[] p, byte[] q);

        /**
         * Subtracts two given Ed25519 points {@code p} and {@code q} and puts the
         * representation of the result into {@code result}.
         *
         * @param result the target array, must be {@link Ed25519#ED25519_BYTES} bytes
         * @param p      the first Ed25519 point, must be {@link Ed25519#ED25519_BYTES}
         *               bytes
         * @param q      the second Ed25519 point, must be {@link Ed25519#ED25519_BYTES}
         *               bytes
         * @return true if successful
         */
        boolean cryptoCoreEd25519Sub(byte[] result, byte[] p, byte[] q);

        /**
         * Creates a random scalar value in {@code [0, l[} with {@code L} being the
         * order of the
         * Ed25519 group.
         *
         * @param scalar the target array, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         */
        void cryptoCoreEd25519ScalarRandom(byte[] scalar);

        /**
         * Reduces a possibly larger scalar value to {@code [0, l[} with {@code L} being
         * the order
         * of the Ed25519 group.
         *
         * @param result the target array, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         * @param scalar the scalar to reduce, must be
         *               {@link Ed25519#ED25519_NON_REDUCED_SCALAR_BYTES}
         *               bytes
         */
        void cryptoCoreEd25519ScalarReduce(byte[] result, byte[] scalar);

        /**
         * Calculates the multiplicative inverse of the given scalar value.
         *
         * @param result the target array, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         * @param scalar the scalar to invert, must be
         *               {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         * @return true if successful
         */
        boolean cryptoCoreEd25519ScalarInvert(byte[] result, byte[] scalar);

        /**
         * Calculates the additive inverse of the given scalar value.
         *
         * @param result the target array, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         * @param scalar the scalar to negate, must be
         *               {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         */
        void cryptoCoreEd25519ScalarNegate(byte[] result, byte[] scalar);

        /**
         * Calculates the result R for the given scalar value such that
         * {@code R + scalar = 1 (mod
         * L)} with {@code L} being the order of the Ed25519 group.
         *
         * @param result the target array, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         * @param scalar the scalar to complement, must be
         *               {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         */
        void cryptoCoreEd25519ScalarComplement(byte[] result, byte[] scalar);

        /**
         * Adds two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the order
         * of the Ed25519 group.
         *
         * @param result the target array, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         * @param x      the first scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         * @param y      the second scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         */
        void cryptoCoreEd25519ScalarAdd(byte[] result, byte[] x, byte[] y);

        /**
         * Subtracts two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the
         * order of the Ed25519 group.
         *
         * @param result the target array, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         * @param x      the first scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         * @param y      the second scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         */
        void cryptoCoreEd25519ScalarSub(byte[] result, byte[] x, byte[] y);

        /**
         * Multiplies two scalars {@code x} and {@code y} modulo {@code L} with
         * {@code L} being the
         * order of the Ed25519 group.
         *
         * @param result the target array, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         * @param x      the first scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         * @param y      the second scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         */
        void cryptoCoreEd25519ScalarMul(byte[] result, byte[] x, byte[] y);
    }

    interface Lazy {

        /**
         * Returns whether the passed encoded string represents a valid Ed25519 point.
         *
         * @param point the point to check
         * @return true if valid
         */
        boolean cryptoCoreEd25519IsValidPoint(String point);

        /**
         * Chooses a random Ed25519 point and returns it.
         *
         * @return a random Ed25519 point
         */
        Ed25519Point cryptoCoreEd25519Random();

        /**
         * Maps a {@link Ed25519#ED25519_HASH_BYTES} bytes hash to a {@link
         * Ed25519Point}.
         *
         * @param hash the encoded hash
         * @return the corresponding Ed25519 point
         */
        Ed25519Point cryptoCoreEd25519FromUniform(String hash) throws SodiumException;

        /**
         * Maps a {@link Ed25519#ED25519_HASH_BYTES} bytes hash to a Ed25519 point.
         *
         * @param hash the hash, must be {@link Ed25519#ED25519_HASH_BYTES}
         * @return the corresponding Ed25519 point
         */
        Ed25519Point cryptoCoreEd25519FromUniform(byte[] hash) throws SodiumException;

        /**
         * Multiplies the given Ed25519 {@code point} by the scalar {@code n} and
         * returns the
         * resulting point.
         *
         * @param n     the scalar
         * @param point the Ed25519 point
         * @return the result
         */
        Ed25519Point cryptoScalarMultEd25519Noclamp(BigInteger n, Ed25519Point point)
                throws SodiumException;

        /**
         * Multiplies the given Ed25519 {@code point} by the scalar {@code n} and
         * returns the
         * resulting point.
         *
         * @param nEnc  the encoded scalar bytes, in little-endian byte order
         * @param point the Ed25519 point
         * @return the result
         */
        Ed25519Point cryptoScalarMultEd25519Noclamp(String nEnc, Ed25519Point point)
                throws SodiumException;

        /**
         * Multiplies the given Ed25519 {@code point} by the scalar {@code n} and
         * returns the
         * resulting point.
         *
         * @param n     the scalar, must be {@link Ed25519#ED25519_BYTES} bytes, in
         *              little-endian encoding
         * @param point the Ed25519 point
         * @return the result
         */
        Ed25519Point cryptoScalarMultEd25519Noclamp(byte[] n, Ed25519Point point)
                throws SodiumException;

        /**
         * Multiplies the Ed25519 base point by the scalar {@code n} and returns the
         * result.
         *
         * @param n the scalar
         * @return the result
         */
        Ed25519Point cryptoScalarMultEd25519BaseNoclamp(BigInteger n) throws SodiumException;

        /**
         * Multiplies the Ed25519 base point by the scalar {@code n} and returns the
         * result.
         *
         * @param nEnc the encoded scalar, in little-endian byte order
         * @return the result
         */
        Ed25519Point cryptoScalarMultEd25519BaseNoclamp(String nEnc) throws SodiumException;

        /**
         * Multiplies the Ed25519 base point by the scalar {@code n} and returns the
         * result.
         *
         * @param n the scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES} bytes, in
         *          little-endian encoding
         * @return the result
         */
        Ed25519Point cryptoScalarMultEd25519BaseNoclamp(byte[] n) throws SodiumException;

        /**
         * Adds two given Ed25519 points {@code p} and {@code q} and returns the result.
         *
         * @param p the first Ed25519 point
         * @param q the second Ed25519 point
         * @return the sum
         */
        Ed25519Point cryptoCoreEd25519Add(Ed25519Point p, Ed25519Point q)
                throws SodiumException;

        /**
         * Subtracts two given Ed25519 points {@code p} and {@code q} and returns the
         * result.
         *
         * @param p the first Ed25519 point
         * @param q the second Ed25519 point
         * @return the difference
         */
        Ed25519Point cryptoCoreEd25519Sub(Ed25519Point p, Ed25519Point q)
                throws SodiumException;

        /**
         * Creates a random scalar value in {@code [0, l[} with {@code L} being the
         * order of the
         * Ed25519 group.
         *
         * @return the random scalar value
         */
        BigInteger cryptoCoreEd25519ScalarRandom();

        /**
         * Reduces a possibly larger scalar value to {@code [0, l[} with {@code L} being
         * the order
         * of the Ed25519 group.
         *
         * @param scalar the scalar to reduce
         * @return the reduced scalar
         */
        BigInteger cryptoCoreEd25519ScalarReduce(BigInteger scalar);

        /**
         * Reduces a possibly larger scalar value to {@code [0, l[} with {@code L} being
         * the order
         * of the Ed25519 group.
         *
         * @param scalarEnc the encoded scalar to reduce
         * @return the reduced scalar
         */
        BigInteger cryptoCoreEd25519ScalarReduce(String scalarEnc);

        /**
         * Reduces a possibly larger scalar value to {@code [0, L[} with {@code L} being
         * the order
         * of the Ed25519 group.
         *
         * @param scalar the scalar to reduce, must be
         *               {@link Ed25519#ED25519_NON_REDUCED_SCALAR_BYTES}
         *               bytes
         * @return the reduced scalar
         */
        BigInteger cryptoCoreEd25519ScalarReduce(byte[] scalar);

        /**
         * Calculates the multiplicative inverse of the given scalar value.
         *
         * @param scalar the scalar to invert
         * @return the multiplicative inverse
         */
        BigInteger cryptoCoreEd25519ScalarInvert(BigInteger scalar) throws SodiumException;

        /**
         * Calculates the multiplicative inverse of the given scalar value.
         *
         * @param scalarEnc the encoded scalar to invert
         * @return the multiplicative inverse
         */
        BigInteger cryptoCoreEd25519ScalarInvert(String scalarEnc) throws SodiumException;

        /**
         * Calculates the multiplicative inverse of the given scalar value.
         *
         * @param scalar the scalar to invert, must be
         *               {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         * @return the multiplicative inverse
         */
        BigInteger cryptoCoreEd25519ScalarInvert(byte[] scalar) throws SodiumException;

        /**
         * Calculates the additive inverse of the given scalar value.
         *
         * @param scalar the scalar to negate
         * @return the additive inverse
         */
        BigInteger cryptoCoreEd25519ScalarNegate(BigInteger scalar);

        /**
         * Calculates the additive inverse of the given scalar value.
         *
         * @param scalarEnc the encoded scalar to negate
         * @return the additive inverse
         */
        BigInteger cryptoCoreEd25519ScalarNegate(String scalarEnc);

        /**
         * Calculates the additive inverse of the given scalar value.
         *
         * @param scalar the scalar to negate, must be
         *               {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         * @return the additive inverse
         */
        BigInteger cryptoCoreEd25519ScalarNegate(byte[] scalar);

        /**
         * Calculates the result R for the given scalar value such that
         * {@code R + scalar = 1 (mod
         * L)} with {@code L} being the order of the Ed25519 group.
         *
         * @param scalar the scalar to complement
         * @return the complement
         */
        BigInteger cryptoCoreEd25519ScalarComplement(BigInteger scalar);

        /**
         * Calculates the result R for the given scalar value such that
         * {@code R + scalar = 1 (mod
         * L)} with {@code L} being the order of the Ed25519 group.
         *
         * @param scalarEnc the encoded scalar to complement
         * @return the complement
         */
        BigInteger cryptoCoreEd25519ScalarComplement(String scalarEnc);

        /**
         * Calculates the result R for the given scalar value such that
         * {@code R + scalar = 1 (mod
         * L)} with {@code L} being the order of the Ed25519 group.
         *
         * @param scalar the scalar to complement, must be
         *               {@link Ed25519#ED25519_SCALAR_BYTES}
         *               bytes
         * @return the complement
         */
        BigInteger cryptoCoreEd25519ScalarComplement(byte[] scalar);

        /**
         * Adds two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the order
         * of the Ed25519 group.
         *
         * @param x the first scalar
         * @param y the second scalar
         * @return the sum
         */
        BigInteger cryptoCoreEd25519ScalarAdd(BigInteger x, BigInteger y);

        /**
         * Adds two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the order
         * of the Ed25519 group.
         *
         * @param x the first scalar
         * @param y the second scalar (encoded)
         * @return the sum
         */
        BigInteger cryptoCoreEd25519ScalarAdd(BigInteger x, String y);

        /**
         * Adds two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the order
         * of the Ed25519 group.
         *
         * @param x the first scalar (encoded)
         * @param y the second scalar
         * @return the sum
         */
        BigInteger cryptoCoreEd25519ScalarAdd(String x, BigInteger y);

        /**
         * Adds two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the order
         * of the Ed25519 group.
         *
         * @param x the first scalar (encoded)
         * @param y the second scalar (encoded)
         * @return the sum
         */
        BigInteger cryptoCoreEd25519ScalarAdd(String x, String y);

        /**
         * Adds two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the order
         * of the Ed25519 group.
         *
         * @param x the first scalar (encoded)
         * @param y the second scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *          bytes
         * @return the sum
         */
        BigInteger cryptoCoreEd25519ScalarAdd(String x, byte[] y);

        /**
         * Adds two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the order
         * of the Ed25519 group.
         *
         * @param x the first scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES} bytes
         * @param y the second scalar (encoded)
         * @return the sum
         */
        BigInteger cryptoCoreEd25519ScalarAdd(byte[] x, String y);

        /**
         * Adds two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the order
         * of the Ed25519 group.
         *
         * @param x the first scalar
         * @param y the second scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *          bytes
         * @return the sum
         */
        BigInteger cryptoCoreEd25519ScalarAdd(BigInteger x, byte[] y);

        /**
         * Adds two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the order
         * of the Ed25519 group.
         *
         * @param x the first scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES} bytes
         * @param y the second scalar
         * @return the sum
         */
        BigInteger cryptoCoreEd25519ScalarAdd(byte[] x, BigInteger y);

        /**
         * Adds two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the order
         * of the Ed25519 group.
         *
         * @param x the first scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES} bytes
         * @param y the second scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *          bytes
         * @return the sum
         */
        BigInteger cryptoCoreEd25519ScalarAdd(byte[] x, byte[] y);

        /**
         * Subtracts two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the
         * order of the Ed25519 group.
         *
         * @param x the first scalar
         * @param y the second scalar
         * @return the difference
         */
        BigInteger cryptoCoreEd25519ScalarSub(BigInteger x, BigInteger y);

        /**
         * Subtracts two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the
         * order of the Ed25519 group.
         *
         * @param x the first scalar
         * @param y the second scalar (encoded)
         * @return the difference
         */
        BigInteger cryptoCoreEd25519ScalarSub(BigInteger x, String y);

        /**
         * Subtracts two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the
         * order of the Ed25519 group.
         *
         * @param x the first scalar (encoded)
         * @param y the second scalar
         * @return the difference
         */
        BigInteger cryptoCoreEd25519ScalarSub(String x, BigInteger y);

        /**
         * Subtracts two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the
         * order of the Ed25519 group.
         *
         * @param x the first scalar (encoded)
         * @param y the second scalar (encoded)
         * @return the difference
         */
        BigInteger cryptoCoreEd25519ScalarSub(String x, String y);

        /**
         * Subtracts two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the
         * order of the Ed25519 group.
         *
         * @param x the first scalar (encoded)
         * @param y the second scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *          bytes
         * @return the difference
         */
        BigInteger cryptoCoreEd25519ScalarSub(String x, byte[] y);

        /**
         * Subtracts two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the
         * order of the Ed25519 group.
         *
         * @param x the first scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES} bytes
         * @param y the second scalar (encoded)
         * @return the difference
         */
        BigInteger cryptoCoreEd25519ScalarSub(byte[] x, String y);

        /**
         * Subtracts two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the
         * order of the Ed25519 group.
         *
         * @param x the first scalar
         * @param y the second scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *          bytes
         * @return the difference
         */
        BigInteger cryptoCoreEd25519ScalarSub(BigInteger x, byte[] y);

        /**
         * Subtracts two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the
         * order of the Ed25519 group.
         *
         * @param x the first scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES} bytes
         * @param y the second scalar
         * @return the difference
         */
        BigInteger cryptoCoreEd25519ScalarSub(byte[] x, BigInteger y);

        /**
         * Subtracts two scalars {@code x} and {@code y} modulo {@code L} with {@code L}
         * being the
         * order of the Ed25519 group.
         *
         * @param x the first scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES} bytes
         * @param y the second scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *          bytes
         * @return the difference
         */
        BigInteger cryptoCoreEd25519ScalarSub(byte[] x, byte[] y);

        /**
         * Multiplies two scalars {@code x} and {@code y} modulo {@code L} with
         * {@code L} being the
         * order of the Ed25519 group.
         *
         * @param x the first scalar
         * @param y the second scalar
         * @return the product
         */
        BigInteger cryptoCoreEd25519ScalarMul(BigInteger x, BigInteger y);

        /**
         * Multiplies two scalars {@code x} and {@code y} modulo {@code L} with
         * {@code L} being the
         * order of the Ed25519 group.
         *
         * @param x the first scalar
         * @param y the second scalar (encoded)
         * @return the product
         */
        BigInteger cryptoCoreEd25519ScalarMul(BigInteger x, String y);

        /**
         * Multiplies two scalars {@code x} and {@code y} modulo {@code L} with
         * {@code L} being the
         * order of the Ed25519 group.
         *
         * @param x the first scalar (encoded)
         * @param y the second scalar
         * @return the product
         */
        BigInteger cryptoCoreEd25519ScalarMul(String x, BigInteger y);

        /**
         * Multiplies two scalars {@code x} and {@code y} modulo {@code L} with
         * {@code L} being the
         * order of the Ed25519 group.
         *
         * @param x the first scalar (encoded)
         * @param y the second scalar (encoded)
         * @return the product
         */
        BigInteger cryptoCoreEd25519ScalarMul(String x, String y);

        /**
         * Multiplies two scalars {@code x} and {@code y} modulo {@code L} with
         * {@code L} being the
         * order of the Ed25519 group.
         *
         * @param x the first scalar (encoded)
         * @param y the second scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *          bytes
         * @return the product
         */
        BigInteger cryptoCoreEd25519ScalarMul(String x, byte[] y);

        /**
         * Multiplies two scalars {@code x} and {@code y} modulo {@code L} with
         * {@code L} being the
         * order of the Ed25519 group.
         *
         * @param x the first scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES} bytes
         * @param y the second scalar (encoded)
         * @return the product
         */
        BigInteger cryptoCoreEd25519ScalarMul(byte[] x, String y);

        /**
         * Multiplies two scalars {@code x} and {@code y} modulo {@code L} with
         * {@code L} being the
         * order of the Ed25519 group.
         *
         * @param x the first scalar
         * @param y the second scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *          bytes
         * @return the product
         */
        BigInteger cryptoCoreEd25519ScalarMul(BigInteger x, byte[] y);

        /**
         * Multiplies two scalars {@code x} and {@code y} modulo {@code L} with
         * {@code L} being the
         * order of the Ed25519 group.
         *
         * @param x the first scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES} bytes
         * @param y the second scalar
         * @return the product
         */
        BigInteger cryptoCoreEd25519ScalarMul(byte[] x, BigInteger y);

        /**
         * Multiplies two scalars {@code x} and {@code y} modulo {@code L} with
         * {@code L} being the
         * order of the Ed25519 group.
         *
         * @param x the first scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES} bytes
         * @param y the second scalar, must be {@link Ed25519#ED25519_SCALAR_BYTES}
         *          bytes
         * @return the product
         */
        BigInteger cryptoCoreEd25519ScalarMul(byte[] x, byte[] y);
    }

    class Checker {
        private Checker() {
        }

        public static void ensurePointFits(byte[] point) {
            if (point == null) {
                throw new IllegalArgumentException(
                        "Null pointers are not allowed as target arrays");
            }

            if (point.length < ED25519_BYTES) {
                throw new IllegalArgumentException(
                        "To hold a Ed25519 point, the array must be "
                                + ED25519_BYTES
                                + " bytes in size");
            }
        }

        public static void ensureScalarFits(byte[] scalar) {
            if (scalar == null) {
                throw new IllegalArgumentException(
                        "Null pointers are not allowed as target arrays");
            }

            if (scalar.length < ED25519_SCALAR_BYTES) {
                throw new IllegalArgumentException(
                        "To hold a Ed25519 scalar, the array must be "
                                + ED25519_SCALAR_BYTES
                                + " bytes in size");
            }
        }

        public static void checkPoint(byte[] point) {
            if (point == null) {
                throw new IllegalArgumentException(
                        "Null pointers are not allowed as Ed25519 points");
            }

            if (point.length != ED25519_BYTES) {
                throw new IllegalArgumentException("A Ed25519 point must be "
                        + ED25519_BYTES
                        + " bytes in size");
            }
        }

        public static void checkHash(byte[] hash) {
            if (hash == null) {
                throw new IllegalArgumentException(
                        "Null pointers are not allowed as Ed25519 hashes");
            }

            if (hash.length != ED25519_HASH_BYTES) {
                throw new IllegalArgumentException("A hash for use with Ed25519 must be "
                        + ED25519_HASH_BYTES
                        + " bytes in size");
            }
        }

        public static void checkScalar(byte[] scalar) {
            if (scalar == null) {
                throw new IllegalArgumentException(
                        "Null pointers are not allowed as Ed25519 scalars");
            }

            if (scalar.length != ED25519_SCALAR_BYTES) {
                throw new IllegalArgumentException("A Ed25519 scalar must be "
                        + ED25519_SCALAR_BYTES
                        + " bytes in size");
            }
        }

        public static void checkNonReducedScalar(byte[] scalar) {
            if (scalar == null) {
                throw new IllegalArgumentException(
                        "Null pointers are not allowed as non-reduced Ed25519 scalars");
            }

            if (scalar.length != ED25519_NON_REDUCED_SCALAR_BYTES) {
                throw new IllegalArgumentException("A non-reduced Ed25519 scalar must be "
                        + ED25519_NON_REDUCED_SCALAR_BYTES
                        + " bytes in size");
            }
        }
    }

    final class Ed25519Point {

        private final LazySodium ls;
        private final byte[] repr;

        private Ed25519Point(LazySodium ls, byte[] repr) {
            if (repr == null || !ls.cryptoCoreEd25519IsValidPoint(repr)) {
                throw new IllegalArgumentException("The passed point is invalid");
            }

            this.repr = repr;
            this.ls = ls;
        }

        private Ed25519Point(LazySodium ls, String encoded) {
            this(ls, ls.decodeFromString(encoded));
        }

        @Override
        public String toString() {
            return encode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (!(o instanceof Ed25519Point)) {
                return false;
            }

            Ed25519Point that = (Ed25519Point) o;
            return Arrays.equals(this.repr, that.repr);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(repr);
        }

        /**
         * Returns the hexadecimal notation of this point's canonical encoding.
         *
         * @return the point in hexadecimal notation
         */
        public String toHex() {
            return ls.toHexStr(repr);
        }

        /**
         * Returns this point's canonical encoding.
         *
         * @return the point
         */
        public byte[] toBytes() {
            return repr;
        }

        /**
         * Encodes the point using the {@link LazySodium}'s associated
         * {@link MessageEncoder}.
         * 
         * @return the encoded point
         */
        public String encode() {
            return ls.encodeToString(repr);
        }

        /**
         * Multiplies this point by a given scalar.
         *
         * @param n the scalar to multiply by
         * @return the multiplied point
         * @throws SodiumException if the result is zero
         */
        public Ed25519Point scalarMult(BigInteger n) throws SodiumException {
            return ls.cryptoScalarMultEd25519Noclamp(n, this);
        }

        /**
         * Multiplies this point by a given scalar.
         *
         * @param n the scalar to multiply by
         * @return the multiplied point
         * @throws SodiumException if the result is zero
         */
        public Ed25519Point times(BigInteger n) throws SodiumException {
            return scalarMult(n);
        }

        /**
         * Adds the given point to this point. Addition is commutative.
         *
         * @param other the point to add
         * @return the sum of both points
         * @throws SodiumException when the operation failed
         */
        public Ed25519Point plus(Ed25519Point other) throws SodiumException {
            return ls.cryptoCoreEd25519Add(this, other);
        }

        /**
         * Subtracts the given point from this point.
         *
         * @param other the point to subtract
         * @return the difference of both points.
         * @throws SodiumException when the operation failed
         */
        public Ed25519Point minus(Ed25519Point other) throws SodiumException {
            return ls.cryptoCoreEd25519Sub(this, other);
        }

        /**
         * Returns the additive inverse of this point. This is equivalent to
         * {@code 0 - p} where 0
         * denotes the additive identity.
         *
         * @return the additive inverse
         * @throws SodiumException when the operation failed.
         */
        public Ed25519Point negate() throws SodiumException {
            return zero(ls).minus(this);
        }

        /**
         * Returns the zero element (identity element) of the Ed25519 group.
         *
         * @param ls the {@link LazySodium} instance
         * @return the identity element of Ed25519
         */
        public static Ed25519Point zero(LazySodium ls) {
            return fromBytes(ls, pointBuffer());
        }

        /**
         * Returns a random element of the Ed25519 group.
         *
         * @param ls the {@link LazySodium} instance
         * @return a random element of Ed25519
         */
        public static Ed25519Point random(LazySodium ls) {
            return ls.cryptoCoreEd25519Random();
        }

        /**
         * Returns the base point of the Ed25519 group.
         *
         * @param ls the {@link LazySodium} instance
         * @return the base point of Ed25519
         */
        public static Ed25519Point base(LazySodium ls) throws SodiumException {
            return ls.cryptoScalarMultEd25519BaseNoclamp(BigInteger.ONE);
        }

        /**
         * Creates a new {@link Ed25519Point} from the hexadecimal representation. The
         * hexadecimal
         * representation must be a valid canonical encoding.
         *
         * @param ls  the {@link LazySodium} instance
         * @param hex the Ed25519 canonical encoding in hexadecimal notation
         * @return the corresponding {@link Ed25519Point}
         */
        public static Ed25519Point fromHex(LazySodium ls, String hex) {
            return new Ed25519Point(ls, ls.toBinary(hex));
        }

        /**
         * Creates a new {@link Ed25519Point} from the encoded representation, using the
         * {@link LazySodium}'s associated {@link MessageEncoder}. The decoded bytes
         * must be a valid
         * canonical encoding.
         *
         * @param ls      the {@link LazySodium} instance
         * @param encoded the encoded Ed25519 point
         * @return the corresponding {@link Ed25519Point}
         */
        public static Ed25519Point fromString(LazySodium ls, String encoded) {
            return new Ed25519Point(ls, encoded);
        }

        /**
         * Creates a new {@link Ed25519Point} from the binary representation. The binary
         * representation must be a valid canonical encoding.
         *
         * @param ls    the {@link LazySodium} instance
         * @param bytes the Ed25519 canonical encoding
         * @return the corresponding {@link Ed25519Point}
         */
        public static Ed25519Point fromBytes(LazySodium ls, byte[] bytes) {
            return new Ed25519Point(ls, bytes);
        }

        /**
         * Maps the encoded input to a {@link Ed25519Point}, using the
         * {@link LazySodium}'s
         * associated {@link MessageEncoder}. The resulting bytes are hashed using
         * SHA-512 and
         * mapped to the Ed25519 group, using {@code crypto_code_ed25519_from_uniform},
         * i.e. the standard hash-to-group algorithm.
         *
         * @param ls           the {@link LazySodium} instance
         * @param encodedInput the encoded bytes
         * @return the mapped {@link Ed25519Point}
         * @throws SodiumException if the mapping failed
         */
        public static Ed25519Point hashToPoint(LazySodium ls, String encodedInput)
                throws SodiumException {
            return hashToPoint(ls, ls.decodeFromString(encodedInput));
        }

        /**
         * Maps the input to a {@link Ed25519Point}, by calculating the SHA-512 hash and
         * mapping it to the Ed25519 group, using
         * {@code crypto_code_ed25519_from_uniform},
         * i.e. the standard hash-to-group algorithm.
         *
         * @param ls    the {@link LazySodium} instance
         * @param input the input bytes
         * @return the mapped {@link Ed25519Point}
         * @throws SodiumException if the mapping failed
         */
        public static Ed25519Point hashToPoint(LazySodium ls, byte[] input)
                throws SodiumException {
            byte[] hash = new byte[Hash.SHA512_BYTES];
            ls.cryptoHashSha512(hash, input, input.length);

            return ls.cryptoCoreEd25519FromUniform(hash);
        }
    }
}
