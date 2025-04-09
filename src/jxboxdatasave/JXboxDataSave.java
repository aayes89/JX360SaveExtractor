package jxboxdatasave;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 *
 * @author Slam
 */
public class JXboxDataSave {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JFileChooser jfc = new JFileChooser("D:\\Content\\0000000000000000\\5841122D\\00000001\\");
        jfc.showOpenDialog(null);
        File dataSFile = jfc.getSelectedFile();
        if (dataSFile != null) {
            System.out.println("PATH: " + dataSFile.getAbsolutePath());
            System.out.println("File Size: " + dataSFile.length() + " bytes");
            try {
                FileInputStream fis = new FileInputStream(dataSFile);
                byte[] fullData = fis.readAllBytes();

                // Parse header
                STFSHeader header = new STFSHeader().parseSTFS(dataSFile);
                displayHeader(header);

                // Extract embedded files
                List<STFSFileEntry> files = parseFileTable(dataSFile);
                displayFileTable(files);

                extractEmbeddedFiles(dataSFile);
                //extractEmbeddedPNG(dataSFile);

                //extractFileTable(fullData);
                //dumpData(fullData); // Full data dump                
                //extractAsciiBlocks(fullData);
                //extractKnownOffsets(fullData);
            } catch (IOException ex) {
                System.err.println("IOE: " + ex.getMessage());
            }
        }
    }

    public static void extractFileTable(byte[] fullData) {
        System.out.println("\n----- EMBEDDED FILES -----");        
        int headerSize = 0x3400; // Value for "CON", (adjust)
        int entrySize = 0x40;    // entry table value
        int numEntries = ByteBuffer.wrap(fullData, 0x340, 2).order(ByteOrder.BIG_ENDIAN).getShort();

        if (numEntries <= 0 || headerSize + numEntries * entrySize > fullData.length) {
            System.out.println("No embedded files detected or invalid table.");
            return;
        }

        for (int i = 0; i < numEntries; i++) {
            int entryOffset = headerSize + (i * entrySize);

            // Nombre del archivo (40 bytes en ASCII)
            StringBuilder fileName = new StringBuilder();
            for (int j = 0; j < 40; j++) {
                byte b = fullData[entryOffset + j];
                if (b == 0) {
                    break;
                }
                fileName.append((char) b);
            }

            // Size of file
            int fileSize = ByteBuffer.wrap(fullData, entryOffset + 0x34, 4).order(ByteOrder.BIG_ENDIAN).getInt();
            // file Offset inside data
            int fileOffset = ByteBuffer.wrap(fullData, entryOffset + 0x38, 4).order(ByteOrder.BIG_ENDIAN).getInt();

            System.out.printf("File: %s | Size: %d bytes | Offset: 0x%X\n",
                    fileName.toString(), fileSize, fileOffset);

            // TODO: extract file to disk
        }
    }

    private static void dumpData(byte[] fullData) {
        System.out.println("--------- HEXDUMP ------------");
        int bytesPerLine = 16;
        for (int i = 0; i < fullData.length; i += bytesPerLine) {
            System.out.printf("[%08X] ", i); // Offset 8 bytes hex
            for (int j = 0; j < bytesPerLine; j++) {
                if (i + j < fullData.length) {
                    System.out.printf("%02X ", fullData[i + j]);
                } else {
                    System.out.print("   "); // fill with space if not 16 bytes
                }
            }
            System.out.print("  | ");
            for (int j = 0; j < bytesPerLine; j++) {
                if (i + j < fullData.length) {
                    byte b = fullData[i + j];
                    char c = (b >= 32 && b <= 126) ? (char) b : '.';
                    System.out.print(c);
                }
            }
            System.out.println();
        }
    }

    /*
    public static void extractAsciiBlocks(byte[] data) {
        System.out.println("\n--------- ASCII STRINGS (>4 chars) ------------");
        int minLength = 4;
        StringBuilder sb = new StringBuilder();
        int start = 0;
        for (int i = 0; i < data.length; i++) {
            byte b = data[i];
            if (b >= 32 && b <= 126) {
                if (sb.length() == 0) {
                    start = i;
                }
                sb.append((char) b);
            } else {
                if (sb.length() >= minLength) {
                    System.out.printf("Offset 0x%08X: \"%s\"\n", start, sb.toString());
                }
                sb.setLength(0);
            }
        }
    }

    public static void extractKnownOffsets(byte[] data) {
        System.out.println("\n--------- KNOWN FIELDS (raw) ------------");

        // Ej: extract 16 bytes from offset 0x344 to get Profile ID
        int profileIdOffset = 0x344;
        if (data.length >= profileIdOffset + 16) {
            System.out.print("Profile ID (0x344): ");
            for (int i = profileIdOffset; i < profileIdOffset + 16; i++) {
                System.out.printf("%02X", data[i]);
            }
            System.out.println();
        }

        // Display Name from offset 0x4110 (usually 32 bytes, UTF-16LE)
        int nameOffset = 0x4110;
        if (data.length >= nameOffset + 64) {
            System.out.print("Display Name (UTF-16LE, 0x4110): ");
            for (int i = nameOffset; i < nameOffset + 64; i += 2) {
                char c = (char) ((data[i + 1] << 8) | (data[i] & 0xFF));
                if (Character.isISOControl(c)) {
                    break;
                }
                System.out.print(c);
            }
            System.out.println();
        }
    }*/
    private static void displayHeader(STFSHeader header) {
        System.out.println("\n----- METADATA -----");
        System.out.println("File Type: " + header.magic);
        System.out.printf("Title ID: %08X%n", header.titleID);
        System.out.print("Profile ID: ");
        for (byte b : header.profileID) {
            System.out.printf("%02X ", b);
        }
        System.out.println();
        System.out.print("Console ID: ");
        for (byte b : header.consoleID) {
            System.out.printf("%02X ", b);
        }
        System.out.println();
        System.out.print("Device ID: ");
        for (byte b : header.deviceID) {
            System.out.printf("%02X ", b);
        }
        System.out.println();
        System.out.println("Title Name: " + header.titleName);
        System.out.println("Description: " + header.description);
        System.out.println("Publisher: " + header.publisher);
    }

    private static List<STFSFileEntry> parseFileTable(File file) throws IOException {
        List<STFSFileEntry> entries = new ArrayList<>();
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(0x340);
            int numEntries = raf.readShort() & 0xFFFF;
            System.out.println("\n----- Extracting embedded files -----");
            System.out.println("Entries on 0x340: " + numEntries);

            if (numEntries <= 0 || numEntries > 1000) {
                System.out.println("No valid file table found. Num entries: " + numEntries);
                return entries;
            }

            // Read file table
            raf.seek(0x342); // Adjust position
            for (int i = 0; i < numEntries; i++) {
                STFSFileEntry entry = new STFSFileEntry();

                // File name (40 bytes, ends with 0x00)
                byte[] nameBytes = new byte[40];
                raf.readFully(nameBytes);
                int nameLength = 0;
                for (int j = 0; j < nameBytes.length; j++) {
                    if (nameBytes[j] == 0) {
                        nameLength = j;
                        break;
                    }
                }
                entry.filename = new String(nameBytes, 0, nameLength, "UTF-8");

                // file size (4 bytes)
                entry.size = raf.readInt();

                // initial bloq (3 bytes)
                byte[] blockBytes = new byte[3];
                raf.readFully(blockBytes);
                entry.startingBlock = ((blockBytes[0] & 0xFF) << 16) | ((blockBytes[1] & 0xFF) << 8) | (blockBytes[2] & 0xFF);

                // jump reserved bytes (adjust to STFS)
                raf.skipBytes(9); // Adjust as format STFS

                entries.add(entry);
            }

            // Read file content
            for (STFSFileEntry entry : entries) {
                long offset = entry.startingBlock * 0x1000L; // 4KB bloqs
                raf.seek(offset);
                entry.content = new byte[entry.size];
                raf.readFully(entry.content);
            }
        }
        return entries;
    }

    private static void displayFileTable(List<STFSFileEntry> files) {
        System.out.println("\n----- EMBEDDED FILES -----");
        if (files.isEmpty()) {
            System.out.println("No embedded files found in standard table.");
        } else {
            for (int i = 0; i < files.size(); i++) {
                STFSFileEntry file = files.get(i);
                System.out.println("File " + (i + 1) + ":");
                System.out.println("  Name: " + file.filename);
                System.out.println("  Size: " + file.size + " bytes");
                System.out.println("  Starting Block: " + String.format("0x%X", file.startingBlock));
            }
        }
    }

    private static void crearDirectorio(File file, String name) {
        // create dir
        File rootDir = new File(file.getParent(), name);
        if (!rootDir.exists()) {
            System.out.println("Creating directory on: " + rootDir.getParent() + " => " + rootDir.mkdir());
        }
    }

    private static void extractEmbeddedFiles(File file) throws IOException {
        String tempDir = "";
        String tempFileName = "";
        // Creating extracting dir
        crearDirectorio(file, "Extracted");

        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] buffer = new byte[(int) raf.length()];
            raf.readFully(buffer);

            System.out.println("\n----- DETECTED EMBEDDED FILES -----");
            int fileCount = 0;

            for (int i = 0; i < buffer.length - 4; i++) {
                String fileType = null;
                int startOffset = i;
                int endOffset = -1;
                String extension = "";

                // Detect PNG
                if (buffer[i] == (byte) 0x89 && buffer[i + 1] == (byte) 0x50
                        && buffer[i + 2] == (byte) 0x4E && buffer[i + 3] == (byte) 0x47) {
                    fileType = "PNG";
                    extension = ".png";
                    for (int j = i; j < buffer.length - 8; j++) {
                        if (buffer[j] == (byte) 0x49 && buffer[j + 1] == (byte) 0x45
                                && buffer[j + 2] == (byte) 0x4E && buffer[j + 3] == (byte) 0x44
                                && buffer[j + 4] == (byte) 0xAE && buffer[j + 5] == (byte) 0x42
                                && buffer[j + 6] == (byte) 0x60 && buffer[j + 7] == (byte) 0x82) {
                            endOffset = j + 8;
                            break;
                        }
                    }
                } // Detect JPEG
                else if (buffer[i] == (byte) 0xFF && buffer[i + 1] == (byte) 0xD8
                        && buffer[i + 2] == (byte) 0xFF) {
                    fileType = "JPEG";
                    extension = ".jpg";
                    for (int j = i; j < buffer.length - 2; j++) {
                        if (buffer[j] == (byte) 0xFF && buffer[j + 1] == (byte) 0xD9) {
                            endOffset = j + 2;
                            break;
                        }
                    }
                } // Detect GIF
                else if (buffer[i] == (byte) 0x47 && buffer[i + 1] == (byte) 0x49
                        && buffer[i + 2] == (byte) 0x46 && buffer[i + 3] == (byte) 0x38) {
                    fileType = "GIF";
                    extension = ".gif";
                    for (int j = i; j < buffer.length - 2; j++) {
                        if (buffer[j] == (byte) 0x00 && buffer[j + 1] == (byte) 0x3B) {
                            endOffset = j + 2;
                            break;
                        }
                    }
                } // Detect directory offset 0xC000-0xC02F , 0xC030 - 0xCFFF files
                else if (i == 0xC000) {
                    RandomAccessFile ranf = raf;
                    ranf.seek(0xC000L);
                    long dDsize = 0xC02FL - 0xC000L; // 47
                    byte[] dataDir = new byte[(int) dDsize];
                    for (int j = 0; j < dataDir.length; j++) {
                        dataDir[j] = ranf.readByte();
                    }
                    tempDir = new String(dataDir, StandardCharsets.UTF_8).replaceAll("[^a-zA-Z0-9\\\\s]", " ").trim();                    
                    //System.out.println(tempDir);
                    //System.out.println("");
                    endOffset = 0xC02F;
                    fileType = "DIR";
                    extension = "";
                } // Detect file into dirs
                else if (i == 0xC030) {
                    RandomAccessFile ranf = raf;
                    ranf.seek(0xC030L);
                    long dDsize = 0xCFFFL - 0xC030L;
                    byte[] dataDir = new byte[(int) dDsize];
                    for (int j = 0; j < dataDir.length; j++) {
                        dataDir[j] = ranf.readByte();
                    }
                    String nfile = new String(dataDir, StandardCharsets.UTF_8).trim();
                    System.out.println("Archivo encontrado: " + dDsize + "\n");
                    tempFileName = nfile.split("Z")[2].trim().split("                               	")[0];
                    System.out.println("Nombre de fichero: "+tempFileName);
                    System.out.println("");
                    endOffset = 0xCFFF;
                    fileType = "FILE";
                    extension = nfile.trim().substring(nfile.indexOf("."), nfile.indexOf(".") + 4);
                    System.out.println("extension: " + extension);
                } // Detect source code - 0xD000
                else if (i == 0xD000) { // #if
                    String code = "";
                    for (int j = i; j < buffer.length - 2; j++) {
                        code += (char) buffer[j];
                        if (buffer[j] == (byte) 0x23 && buffer[j + 1] == (byte) 0x65 && buffer[j + 2] == (byte) 0x6E
                                && buffer[j + 3] == (byte) 0x64 && buffer[j + 4] == (byte) 0x69 && buffer[j + 5] == (byte) 0x66
                                && buffer[j + 6] == (byte) 0x0D && buffer[j + 7] == (byte) 0x0A) { // #endif
                            endOffset = j + 8;
                            break;
                        }
                    }
                    System.out.println("Source Code found:\n" + code);
                    fileType = "CODE";
                    extension = ".txt";
                }

                if (fileType != null && endOffset != -1) {
                    fileCount++;
                    int fileSize = endOffset - startOffset;
                    System.out.println("File " + fileCount + ": " + fileType);
                    System.out.println("  Offset: 0x" + String.format("%X", startOffset));
                    System.out.println("  Size: " + fileSize + " bytes");

                    // Preguntar si extraer
                    int response = JOptionPane.showConfirmDialog(null,
                            "Extract " + fileType + " at 0x" + String.format("%X", startOffset) + " (" + fileSize + " bytes)?",
                            "Extract File", JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        String outputPath = file.getParent() + File.separator + "Extracted" + File.separator + "extracted_file_" + fileCount + extension;
                        if (fileType.equals("DIR")) {                            
                            crearDirectorio(file, "Extracted" + File.separator + tempDir);
                            tempDir = file.getParent() + File.separator + "Extracted" + File.separator + tempDir;
                        } else if (fileType.equals("FILE")) {
                            if (!tempDir.isEmpty()) {
                                outputPath = tempDir + File.separator + tempFileName;
                                try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                                    fos.write(buffer, startOffset, fileSize);
                                    System.out.println("  Extracted to: " + outputPath);
                                }
                            }
                        } else if(fileType.equals("CODE")){
                            outputPath = file.getParent() + File.separator + "Extracted" + File.separator + "extracted_source_code_" + fileCount + extension;
                            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                                fos.write(buffer, startOffset, fileSize);
                                System.out.println("  Extracted to: " + outputPath);
                            }
                        }
                        else {
                            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                                fos.write(buffer, startOffset, fileSize);
                                System.out.println("  Extracted to: " + outputPath);
                            }
                        }
                    } else {
                        System.out.println("  Skipped extraction.");
                    }
                    i = endOffset - 1; // jump to EOF
                }
            }

            if (fileCount == 0) {
                System.out.println("No additional embedded files found.");
            }
        }
    }
}
