package peerlink.utils;

import java.util.Optional;

public class HttpRequestBodyParser {
    public static Optional<ParsedData> parse(byte[] data, String boundary){
        try {
            String dataAsString = new String(data);
            String fileNameMarker = "filename=\"";
            int fileNameStart = dataAsString.indexOf(fileNameMarker);
            if (fileNameStart == -1){
                return Optional.empty();
            }
            fileNameStart += fileNameMarker.length();
            int fileNameEnd = dataAsString.indexOf("\"",fileNameStart);
            String fileName = dataAsString.substring(fileNameStart,fileNameEnd);

            String contentTypeMarker = "Content-Type: ";
            int contentTypeStart = dataAsString.indexOf(contentTypeMarker,fileNameEnd);
            String contentType = "application/octet-stream";
            if (contentTypeStart != -1){
                contentTypeStart += contentTypeMarker.length();
                int contentTypeEnd = dataAsString.indexOf("\r\n",contentTypeStart);
                contentType = dataAsString.substring(contentTypeStart,contentTypeEnd);
            }

            String headerEndMarker = "\r\n\r\n";
            int headerEnd = dataAsString.indexOf(headerEndMarker);
            if (headerEnd == -1){
                return Optional.empty();
            }
            int contentStart = headerEnd + headerEndMarker.length();
            byte[] boundaryBytes = ("\r\n--"+boundary+"--").getBytes();
            int contentEnd = findSequence(data,boundaryBytes,contentStart);
            if (contentEnd == -1){
                boundaryBytes = ("\r\n--"+boundary).getBytes();
                contentEnd = findSequence(data,boundaryBytes,contentStart);
            }
            if (contentEnd == -1 || contentEnd < contentStart){
                return Optional.empty();
            }
            byte[] fileContent = new byte[contentEnd-contentStart];
            System.arraycopy(data,contentStart,fileContent,0,fileContent.length);
            return Optional.of(new ParsedData(fileName,fileContent,contentType));
        }catch (Exception e){
            System.out.println("Error parsing multipart data : "+e.getMessage());
            return Optional.empty();
        }
    }

    private static int findSequence(byte[] data, byte[] sequence, int start){
        for (int i = start; i <= data.length - sequence.length; i++) {
            boolean match = true;
            for (int j = 0; j < sequence.length; j++) {
                if (data[i + j] != sequence[j]) {
                    match = false;
                    break;
                }
            }
            if (match) return i;
        }
        return -1;
    }

    public static class ParsedData{
        private final String fileName;
        private final byte[] fileContent;
        private final String contentType;

        public ParsedData(String fileName, byte[] fileContent, String contentType) {
            if (fileName != null && fileContent != null && contentType != null){
                this.fileName = fileName;
                this.fileContent = fileContent;
                this.contentType = contentType;
            }else throw new IllegalArgumentException("Please pass valid Arguments to ParsedData Constructor");
        }

        public String getFileName() {
            return fileName;
        }

        public byte[] getFileContent() {
            return fileContent;
        }

        public String getContentType() {
            return contentType;
        }
    }
}
