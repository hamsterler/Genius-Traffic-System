package tofgui_v2.Model;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.imageio.ImageIO;

public class MJPEG extends Thread
{
    public MJPEG(String url, String user, String password)
    {
        this._url = url;       
        this._authorization = true;
        this._encoded = Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));  //Java 8
    }   
    
    public MJPEG(String url)
    {
        this._url = url;       
    }   
    
    @Override
    public void run()
    {
        while (true) 
        {
            this._connected = false;
            
            try 
            {
                URL u = new URL(this._url);
                URLConnection urlConn = u.openConnection();
                urlConn.setUseCaches(false);
                if (this._authorization) urlConn.setRequestProperty("Authorization", "Basic " + this._encoded);
                urlConn.setReadTimeout(1000);
                urlConn.connect();
                urlStream = urlConn.getInputStream();
                stringWriter = new StringWriter(256);
            } 
            catch (Exception ex) { }
            
            while (true) 
            {
                try 
                {
                    retrieveNextImage();
                    this._connected = true;
                } 
                catch (Exception ex) { break; }
                //try  { Thread.sleep(1); }  catch (InterruptedException ex) { }
            }
            
            this._connected = false;
            System.out.println("disconnected");

            // close streams
            try { urlStream.close(); } 
            catch (IOException ioe) { System.err.println("Failed to close the stream: " + ioe); }        
            
            try  { Thread.sleep(100); }  catch (InterruptedException ex) { }
        }
    }
        
    private void retrieveNextImage() throws IOException
    {        
		int currByte = -1;
		
		String header = null;
                stringWriter = new StringWriter(256);
                
                int count = 0;
                int contentLength = 0;
                while((currByte = urlStream.read()) > -1)
		{
                    stringWriter.write(currByte);
			
                    String tempString = stringWriter.toString();
                    int indexOf = tempString.indexOf(END_HEADER);
                    if(indexOf > 0)
                    {
			header = tempString;                        
                                                
                        contentLength = contentLength(header);
                        if (contentLength > 0) break;
                    }
                    count++;
                    if (count > 256) { stringWriter.close(); return; }
                    stringWriter.close();
                }
                
                while (true)
                {
                    if (urlStream.read() == 0xFF)
                    {
                        if (urlStream.read() == 0xD8) break;
                    }
                }
		                
                if (contentLength > 0)
                {
                    this._image_buffer[0] = (byte)0xFF;
                    this._image_buffer[1] = (byte)0xD8;

                    int offset = 2;
                    int numRead = 0;
                    
                    while (offset < contentLength && 
                          (numRead=urlStream.read(this._image_buffer, offset, contentLength-offset)) >= 0) 
                    {
                        offset += numRead;
                    }                      
                    
                    InputStream in = new ByteArrayInputStream(this._image_buffer, 0, contentLength);
                    this._bitmap = ImageIO.read(in);
                    in.close();
                } 
        }

	// dirty but it works content-length parsing
	private int contentLength(String header)
	{
            header = header.toLowerCase();
		int indexOfContentLength = header.indexOf(CONTENT_LENGTH);
		int valueStartPos = indexOfContentLength + CONTENT_LENGTH.length();
		int indexOfEOL = header.indexOf('\n', indexOfContentLength);
		
		String lengthValStr = header.substring(valueStartPos, indexOfEOL).trim();
		
		int retValue = Integer.parseInt(lengthValStr);
		
		return retValue;
	} 
        
    private static final String CONTENT_LENGTH = "content-length:";
    private static final String END_HEADER = "\r\n\r\n";
        
    private InputStream urlStream;
    private StringWriter stringWriter;
        
    private byte[] _image_buffer = new byte[640*480*3]; // maximum size at VGA
    
    private boolean _authorization = false;
    private String _encoded = "";
    
    private int count = 0;
        
    public String getURL() { return this._url; }
    private String _url = "";
    
    public BufferedImage getBitmap() { return this._bitmap; }
    private BufferedImage _bitmap = null;
    
    public boolean isConnected() { return this._connected; } 
    private boolean _connected = false;
}
