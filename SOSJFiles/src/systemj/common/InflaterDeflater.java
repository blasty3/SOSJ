package systemj.common;  
 import java.io.ByteArrayOutputStream;   
 import java.io.IOException;  
 import java.util.zip.DataFormatException;  
 import java.util.zip.Deflater;  
 import java.util.zip.Inflater;  
 public class InflaterDeflater {  
  public static synchronized byte[] compress(byte[] data) throws IOException {  
   Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
   deflater.setInput(data);  
   ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);   
   deflater.finish();  
   byte[] buffer = new byte[1024];   
   while (!deflater.finished()) {  
    int cnt = deflater.deflate(buffer);
    outputStream.write(buffer, 0, cnt);   
   }  
   outputStream.close();  
   byte[] output = outputStream.toByteArray();  
   return output;  
  }  
  public static synchronized byte[] decompress(byte[] data) throws IOException, DataFormatException {  
   Inflater inflater = new Inflater();
   inflater.setInput(data);  
   ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);  
   byte[] buffer = new byte[1024];  
   while (!inflater.finished()) {  
    int cnt = inflater.inflate(buffer);  
    outputStream.write(buffer, 0, cnt);  
   }  
   outputStream.close();  
   byte[] output = outputStream.toByteArray();
   return output;  
  }  
 }