package speech.asr.utils;

public enum PCMFormat {
    F32BE(32, PCMEncoding.FLOAT, false),
    F32LE(32, PCMEncoding.FLOAT, true),
    F64BE(64, PCMEncoding.FLOAT, false),
    F64LE(64, PCMEncoding.FLOAT, true),
    S16BE(16, PCMEncoding.SIGNED, false),
    S16LE(16, PCMEncoding.SIGNED, true),
    S24BE(24, PCMEncoding.SIGNED, false),
    S24LE(24, PCMEncoding.SIGNED, true),
    S32BE(32, PCMEncoding.SIGNED, false),
    S32LE(32, PCMEncoding.SIGNED, true),
    S8(8, PCMEncoding.SIGNED, true),
    U16BE(16, PCMEncoding.UNSIGNED, false),
    U16LE(16, PCMEncoding.UNSIGNED, true),
    U24BE(24, PCMEncoding.UNSIGNED, false),
    U24LE(24, PCMEncoding.UNSIGNED, true),
    U32BE(32, PCMEncoding.UNSIGNED, false),
    U32LE(32, PCMEncoding.UNSIGNED, true),
    U8(8, PCMEncoding.UNSIGNED, true);

    int sampleSize;
    PCMEncoding encoding;
    boolean littleEndian;

    PCMFormat(int sampleSize, PCMEncoding encoding, boolean littleEndian) {
        this.sampleSize = sampleSize;
        this.encoding = encoding;
        this.littleEndian = littleEndian;
    }

    public PCMEncoding getEncoding() {
        return encoding;
    }

    public boolean isLittleEndian() {
        return littleEndian;
    }

    public int getSampleSize() {
        return sampleSize;
    }
}
