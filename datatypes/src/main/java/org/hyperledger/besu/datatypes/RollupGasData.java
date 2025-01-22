package org.hyperledger.besu.datatypes;

import org.apache.tuweni.bytes.Bytes;

/**
 * Optimism roll up data records.
 */
public record RollupGasData(long zeroes, long ones, long fastLzSize) {

    private static final RollupGasData empty = new RollupGasData(0L, 0L, 0L);

    /**
     * Get the number of zeroes.
     *
     * @return the number of zeroes
     */
    @Override
    public long zeroes() {
        return zeroes;
    }

    /**
     * Get the number of non-zeroes.
     *
     * @return the number of non-zeroes
     */
    @Override
    public long ones() {
        return ones;
    }

    /**
     * Get the number of fast-lz size.
     *
     * @return the number of fast-lz size
     */
    @Override
    public long fastLzSize() {
        return fastLzSize;
    }

    /**
     * Create a new roll up data record from a payload bytes.
     *
     * @param payload the transaction rlp encoded bytes
     * @return the roll up data record
     */
    public static RollupGasData fromPayload(final Bytes payload) {
        if (payload == null) {
            return empty;
        }
        final int length = payload.size();
        int zeroes = 0;
        int ones = 0;
        for (int i = 0; i < length; i++) {
            byte b = payload.get(i);
            if (b == 0) {
                zeroes++;
            } else {
                ones++;
            }
        }
        var fastLzSize = flzCompressLength(payload.toArray());
        return new RollupGasData(zeroes, ones, fastLzSize);
    }

    /**
     * FlzCompressLen returns the length of the data after compression through FastLZ, based on <a
     * href="">https://github.com/Vectorized/solady/blob/5315d937d79b335c668896d7533ac603adac5315/js/solady.js</a>
     */
    static long flzCompressLength(final byte[] ib) {
        var n = 0;
        var ht = new int[8192];

        var a = 0;
        var ipLimit = ib.length > 13 ? ib.length - 13 : 0;
        for (var ip = a + 2; ip < ipLimit; ) {
            var r = 0;
            var d = 0;
            for (; ; ) {
                var s = u24(ib, ip);
                var h = hash(s);
                r = ht[h];
                ht[h] = ip;
                d = ip - r;
                if (ip >= ipLimit) {
                    break;
                }
                ip++;
                if (d <= 0x1fff && s == u24(ib, r)) {
                    break;
                }
            }
            if (ip >= ipLimit) {
                break;
            }
            ip--;
            if (ip > a) {
                n = literals(n, ip - a);
            }
            var l = cmp(ib, r + 3, ip + 3, ipLimit + 9);
            n = match(n, l);
            ip = setNextHash(ib, ht, setNextHash(ib, ht, ip + l));
            a = ip;
        }
        n = literals(n, ib.length - a);
        return n;
    }

    static int u24(final byte[] ib, final int i) {
        return (ib[i] & 0xFF) | (ib[i + 1] & 0xFF) << 8 | (ib[i + 2] & 0xFF) << 16;
    }

    static int cmp(final byte[] ib, final int p, final int q, final int e) {
        var loopCount = 0;
        var value = e;
        for (value = value - q; loopCount < value; loopCount++) {
            if ((ib[p + loopCount] & 0xFF) != (ib[q + loopCount] & 0xFF)) {
                value = 0;
            }
        }
        return loopCount;
    }

    static int literals(final int n, final int r) {
        var a = n;
        var b = r;

        a += 0x21 * (b / 0x20);
        b %= 0x20;
        if (b != 0) {
            a += b + 1;
        }
        return a;
    }

    static int match(final int n, final int l) {
        var a = n;
        var b = l;
        b--;
        a += 3 * (b / 262);
        if (b % 262 >= 6) {
            a += 3;
        } else {
            a += 2;
        }
        return a;
    }

    static int hash(final int v) {
        return (int) ((2654435769L * v) >> 19) & 0x1fff;
    }

    static int setNextHash(final byte[] ib, final int[] ht, final int ip) {
        ht[hash(u24(ib, ip))] = ip;
        return ip + 1;
    }
}
