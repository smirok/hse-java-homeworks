package ru.itmo.mit.git.utils.compressor;

import com.github.luben.zstd.Zstd;

public class ZstdCompressor {
    public static byte[] compress(byte[] source) {
        return Zstd.compress(source);
    }

    public static byte[] decompress(byte[] source) {
        return Zstd.decompress(source, (int) Zstd.decompressedSize(source));
    }
}
