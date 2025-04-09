package jxboxdatasave;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Slam
 */
public class STFSHeader {
    String magic;
    int titleID;
    byte[] profileID = new byte[8];
    byte[] consoleID = new byte[5];
    byte[] deviceID = new byte[20];
    String titleName;
    String description;
    String publisher;

    public STFSHeader parseSTFS(File file) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            STFSHeader header = new STFSHeader();

            // Magic
            byte[] magicBytes = new byte[4];
            raf.readFully(magicBytes);
            header.magic = new String(magicBytes).trim();
            if (!header.magic.equals("CON") && !header.magic.equals("LIVE") && !header.magic.equals("PIRS")) {
                throw new IOException("No es un contenedor STFS válido: Magic encontrado = \"" + header.magic + "\"");
            }

            // Title ID
            raf.seek(0x360);
            header.titleID = raf.readInt();

            // Profile ID
            raf.seek(0x344);
            raf.readFully(header.profileID);

            // Console ID
            raf.seek(0x9);
            raf.readFully(header.consoleID);

            // Device ID
            raf.seek(0x3B1);
            raf.readFully(header.deviceID);

            // Title Name
            raf.seek(0x411);
            byte[] title = new byte[128];
            raf.readFully(title);
            int titleLength = 0;
            for (int i = 0; i < title.length - 1; i += 2) {
                if (title[i] == 0 && title[i + 1] == 0) {
                    titleLength = i;
                    break;
                }
            }
            if (titleLength == 0) titleLength = title.length;
            header.titleName = new String(title, 0, titleLength, StandardCharsets.UTF_8).trim();

            // Description
            raf.seek(0xD12);
            byte[] desc = new byte[128];
            raf.readFully(desc);
            header.description = new String(desc, "UTF-16LE").trim();

            // Publisher
            raf.seek(0x1690);
            byte[] pub = new byte[128];
            raf.readFully(pub);
            header.publisher = new String(pub, "UTF-16LE").trim();

            return header;
        }
    }
}