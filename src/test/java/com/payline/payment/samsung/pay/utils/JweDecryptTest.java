package com.payline.payment.samsung.pay.utils;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JweDecryptTest {

    private static final String ciphered = "eyJhbGciOiJSU0ExXzUiLCJraWQiOiJZeDFDQWR2eXg0NXFDN0xlVXVBaXQwTWRpZE4yV3hKNW11R2VVaDNCelRnPSIsInR5cCI6IkpPU0UiLCJjaGFubmVsU2VjdXJpdHlDb250ZXh0IjoiUlNBX1BLSSIsImVuYyI6IkExMjhHQ00ifQ.YCLbkceDX0Rbpke9znVY-pxc-MaB6h4siWhBEnAR7l0Q2Qm8Lmb48haUra6XutLbNb-CvRFJtf04FVD_MR7a6DQQOqXWclzA0mh9Hd6LZ0DLJMG2jqFlQ6zW5JSxsazexGlrwS40o2UQ1netiAc076HAaK3iG5bBQO-7Z75gfRJlUU0wi9JVNxkItvBSC18xoxTc0Malhtsg7ZcqPE51lfbzmelnPxU4x5MmtvGvDIPup7oHNNA3C1B_AeIu9fvK8LfU8gOUbHHrzrW-9-97JJvSePEZLMinN9cqO-Pzdq5z6JY-ZJTT0aj57DxrBgrF2kqXcu6Ei7JyL_UZKXAnsQ.ltnK6haiGdW3oSzj.ebUM8TQZ5LfrT3LUVcJd0IyjAhv9-QQKMBLDyyWC5PiPmq0h5ibfaTnbMAN3U8PmhWO9g34S6NPRfwqKPaPHv5Ii2wDP5cSum8uTK3vkn4yyibzAAiZgOcAc2g74wGP1IrqUMyx4ukohzwRfPICdV0UNaaj-DtYfl7b4AKppE-KrspnOpGkpvWVHQySMDRSd9aKogIU5bJ2oDJ3SzAggFPqtg78GtQH9uaKXGgPOFnftBFQOPEbe.8t0y2EsB3wSv--dv-6865g";
    private static final String clear = "{\"amount\":\"100\",\"cryptogram\":\"AgAAAAAABQrqUtnic6MLQAAAAAA=\",\"currency_code\":\"USD\",\"eci_indicator\":\"07\",\"tokenPanExpiration\":\"1225\",\"utc\":\"1542290658225\",\"tokenPAN\":\"4895370013341927\"}";

    private JweDecrypt service = JweDecrypt.getInstance();

    @Test
    public void getDecryptedData() throws Exception {

        // load the private key file
        byte[] privKey = Files.readAllBytes(Paths.get(JweDecrypt.class.getClassLoader().getResource("keystore/encodedPrivateKey.txt").toURI()));
        String res = service.getDecryptedData(ciphered, privKey);
        assertEquals(clear, res);
    }
}
