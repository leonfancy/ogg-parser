package me.chenleon.media.audio.opus;

import com.google.common.io.LittleEndianDataInputStream;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

@Setter
@Getter
public class IdHeader {
    public static final byte[] MAGIC_SIGNATURE = {'O', 'p', 'u', 's', 'H', 'e', 'a', 'd'};
    private int majorVersion;
    private int minorVersion;
    private int channelCount;
    private int preSkip;
    private double outputGain;
    private int coupledCount;
    private long inputSampleRate;
    private int channelMappingFamily;
    private int streamCount;
    private int[] channelMapping;

    private IdHeader() {}

    public static IdHeader from(byte[] data) throws IOException {
        IdHeader idHeader = new IdHeader();
        LittleEndianDataInputStream in = new LittleEndianDataInputStream(new ByteArrayInputStream(data));
        if(!Arrays.equals(in.readNBytes(8), MAGIC_SIGNATURE)) {
            throw new InvalidOpusException("Id Header Packet not starts with 'OpusHead'");
        }
        byte version = in.readByte();
        idHeader.majorVersion = version >> 4;
        idHeader.minorVersion = version & 0x0F;
        idHeader.channelCount = in.readUnsignedByte();
        if(idHeader.channelCount < 1) {
            throw new InvalidOpusException("Invalid channel count: "  + idHeader.channelCount);
        }
        idHeader.preSkip = in.readUnsignedShort();
        idHeader.inputSampleRate = Integer.toUnsignedLong(in.readInt());
        idHeader.outputGain = in.readUnsignedShort() / 256.0;
        idHeader.channelMappingFamily = in.readUnsignedByte();
        if (idHeader.channelMappingFamily == 0) {
            if(idHeader.channelCount > 2) {
                throw new InvalidOpusException("Channel count must not be more than 2 for channel mapping family 0. Current channel count is: " + idHeader.channelCount);
            }
            idHeader.streamCount = 1;
            idHeader.coupledCount = idHeader.channelCount - idHeader.streamCount;
            idHeader.channelMapping = idHeader.channelCount == 1 ? new int[]{0} : new int[]{0, 1};
        } else {
            idHeader.streamCount = in.readUnsignedByte();
            idHeader.coupledCount = in.readUnsignedByte();
            idHeader.channelMapping = new int[idHeader.channelCount];
            for (int i = 0; i < idHeader.channelCount; i++) {
                idHeader.channelMapping[i] = in.readUnsignedByte();
            }
        }
        return idHeader;
    }
}
