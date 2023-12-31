package io.github.rainvaporeon.chess.fish.game.utils.board;

import io.github.rainvaporeon.chess.fish.game.Piece;
import com.spiritlight.fishutils.misc.arrays.primitive.LongArray;

// TODO: For each touching square (see: Piece), see the corresponding
// TODO: masks that are not touching the same side and clear the attacks
// TODO: from those squares, and then multiply by magic and shift the
// TODO: required bits to find the valid squares this may go to, in a Nth
// TODO: significant bit type. This may be slightly complex to solve, however.
public class Magic {
    private static final long[] ROOK_MAGIC = {
            0xc80001828100040L,
            0x26004408400010L,
            0x1060040000202048L,
            0x110141100800888L,
            0x84420501a000802L,
            0x1803002905002224L,
            0x104380106000182L,
            0x208201001041L,
            0x4080118100002020L,
            0x1c40120100004020L,
            0x1009040000802020L,
            0x884002800841010L,
            0x2220068801201011L,
            0x1911442000004022L,
            0x4020802010011L,
            0x220004400102001L,
            0x4009608000842280L,
            0x4060104318400210L,
            0x811002400040020L,
            0x4801520000c41010L,
            0x4280020022204L,
            0x413000408404041L,
            0xc028800000101L,
            0x100400e904000201L,
            0x3800242000100a5L,
            0xc0008008210321L,
            0x2003002400001261L,
            0x410028894a041001L,
            0x800041040002901L,
            0x1002080204080eL,
            0x22218040040401L,
            0x24084103280482L,
            0x20002020801040L,
            0x1020200020400812L,
            0x4082000000c0061L,
            0x4042024001200804L,
            0x5040040000024188L,
            0x100040008101a13L,
            0x408108018080802L,
            0x80408064000041L,
            0x850400821800120L,
            0x8100240008480bL,
            0x6008080001100241L,
            0x1eb0100200110248L,
            0x30202216010004L,
            0x802021000c6142L,
            0x32200444020410aL,
            0xa0202004580041L,
            0x80410000406180L,
            0x8220200002040a3L,
            0x93000200a002804L,
            0x40080080010010b2L,
            0x40080101012a1304L,
            0x4041000001012L,
            0x10001000e840482L,
            0xc04020200004885L,
            0x1080006110001041L,
            0x2081211102084250L,
            0x1020001500014619L,
            0x3810440a10000c23L,
            0x410484200860422L,
            0x180b0201090b2004L,
            0x1100040124410282L,
            0x1820a101000443L,
    };

    private static final long[] BISHOP_MAGIC = {
            0x1841160051a00401L,
            0x202000012224a02L,
            0x284003005412542L,
            0x2800a220c40502L,
            0x68005050040308L,
            0x440802c810020230L,
            0x240034002014417L,
            0x45202411310208aL,
            0x1a08410602016172L,
            0x830464044100e08L,
            0x411a31a010040808L,
            0x200124000484405L,
            0x60020c80000120aL,
            0x6a001002583420aL,
            0x10082020084c0051L,
            0x1325202008030692L,
            0x2002026a00045020L,
            0x41051c000052122cL,
            0x10200b000100803L,
            0x720204000610032L,
            0x6818603041604002L,
            0x1041040010020001L,
            0x301081420025204L,
            0x42024420826001L,
            0x404304000131801L,
            0x1041090c00842100L,
            0x144110020080240L,
            0x1211004002202008L,
            0x105080000500401L,
            0x2080600c62080131L,
            0x844100114022082L,
            0x841080020804b00L,
            0x8240800080210L,
            0x4002144012084210L,
            0x2052000060904L,
            0x88400420500c14L,
            0x434000204404L,
            0x1840240800212081L,
            0x4208c019244184L,
            0x54da10030841L,
            0x1200090011080a02L,
            0x800140c000414c8L,
            0x4400382000401058L,
            0x100020140000105L,
            0x610040040044421L,
            0x20001202848110aL,
            0x222011010300101L,
            0x106608008012304L,
            0x220429002014a02L,
            0x110800a005104208L,
            0x806201048200111L,
            0x2082008a03000802L,
            0x2041008034011420L,
            0x401006005004434L,
            0x90a429004080204L,
            0x2041040950041802L,
            0x1002200028441145L,
            0x109004012802018aL,
            0x65100405500801L,
            0x284240458180a00L,
            0x2002441020024020L,
            0x4a10110101144112L,
            0x1010a2c00005030L,
            0x40c012121020424L,
    };

    private static final LongArray ROOK = LongArray.fromArray(ROOK_MAGIC);

    private static final LongArray BISHOP = LongArray.fromArray(BISHOP_MAGIC);

    public static LongArray rookMagic() {
        return ROOK;
    }

    public static LongArray bishopMagic() {
        return BISHOP;
    }

    public static long get(int type, int index) {
        return switch (type) {
            case Piece.ROOK -> ROOK_MAGIC[index];
            case Piece.BISHOP -> BISHOP_MAGIC[index];
            default -> throw new IllegalArgumentException(STR."For given piece type \{Piece.asString(type)}");
        };
    }

    public static String visualize(long pattern) {
        return BoardHelper.visualize(pattern);
    }
}
