import java.io.*;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Toolkit;
import java.net.URL;
import java.net.MalformedURLException;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.*;
import javax.swing.ImageIcon;
import javax.imageio.ImageIO;

public class ImageSplitter extends JPanel implements ActionListener {
	
    static private final String newline = "\n";
    JButton openButton, splitButton;
    JTextArea log, splitDimensionsField;
    JFileChooser fc;
	Image image;
	BufferedImage bImage;

    public ImageSplitter() {
        super(new BorderLayout());

        // status messages
        log = new JTextArea(5,20);
        log.setMargin(new Insets(5,5,5,5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

        // create a file chooser
        fc = new JFileChooser(System.getProperty("user.home"));

		// open button
        openButton = new JButton("Open an Image...");
        openButton.addActionListener(this);

        // split button
        splitButton = new JButton("Split Image");
        splitButton.addActionListener(this);
		
		// dimensions textarea
		splitDimensionsField = new JTextArea("dimensions (ROWSxCOLUMNS)",1,20);

        // layout stuff
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(openButton);
        buttonPanel.add(splitButton);
		
		JPanel textFieldPanel = new JPanel();
		textFieldPanel.add(splitDimensionsField);

        // add panels
        add(buttonPanel, BorderLayout.PAGE_START);
		add(textFieldPanel, BorderLayout.CENTER);
        add(logScrollPane, BorderLayout.PAGE_END);
    }

    public void actionPerformed(ActionEvent e) {

        //Handle open button action.
        if (e.getSource() == openButton) {
            int returnVal = fc.showOpenDialog(ImageSplitter.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                log.append("Opening: " + file.getName() + "." + newline);
				fileToBufferedImage(file);
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());

        //Handle split button action.
        } else if (e.getSource() == splitButton) {
			bufferedImageSplit(bImage);
            log.setCaretPosition(log.getDocument().getLength());
        } 
    }

	public void bufferedImageSplit(BufferedImage a) {
		int w = bImage.getWidth();
		int h = bImage.getHeight();
		int[][] array = new int[w][h];
		System.out.println(w + " " + h);
		// load rgb values into 2d array
		for (int j = 0; j < w; j++) {
			for (int k = 0; k < h; k++) {
				array[j][k] = bImage.getRGB(j, k);
			}
		}
		
		// get dimensions
		String[] dims = splitDimensionsField.getText().split("x");
		try{
			int columns = Integer.parseInt(dims[1]);
			int rows = Integer.parseInt(dims[0]);
			int mh = h/rows; // height of each chunk
			int mw = w/columns; // width of each chunk
			System.out.println(mw + " " + mh);
			int offw = 0;
			int offh = 0;
			/**/
			for(int k=1;k<=rows*columns;k++){
				String path = k+".png";
				BufferedImage image2 = new BufferedImage(mw, mh, BufferedImage.TYPE_INT_RGB);
				//process chunk
				for (int x = 0; x < mw; x++) {
					for (int y = 0; y < mh; y++) {
						try{
							image2.setRGB(x, y, array[x+offw][y+offh]);
						}catch(ArrayIndexOutOfBoundsException e){
							
						}
					}
				}
				//save chunk to image
				File ImageFile = new File(path);
				try {
					ImageIO.write(image2, "png", ImageFile);
					log.append("Saved image " + k + ".png." + newline);
				} catch (IOException e) {
					e.printStackTrace();
				}
				log.setCaretPosition(log.getDocument().getLength());
				
				//if on final one in row
				if(k%columns == 0){
					//set back to beginning
					offw = 0;
					//move down 1
					offh += mh;
				}else{
					offw += mw;
				}
			}
		/**/
		}catch(ArrayIndexOutOfBoundsException e){
			log.append("Error with dimensions." + newline);
		}
		log.setCaretPosition(log.getDocument().getLength());
	}
	
	public void fileToBufferedImage(File a) {
		try {
			bImage = ImageIO.read(a);
		} catch (IOException e) {
			log.append("Error with file." + newline);
		}
		log.setCaretPosition(log.getDocument().getLength());
	}
	
	public void urlToBufferedImage(String a) {
		/*url to image*/
		try {
			URL url = new URL(a);
			image = Toolkit.getDefaultToolkit().createImage(url);
			imageToBufferedImage(image);
		} catch (MalformedURLException e) {
			log.append("Invalid URL" + newline);
		}
		log.setCaretPosition(log.getDocument().getLength());
		/*image to buffered image*/
		imageToBufferedImage(image);
	}
	
	public void imageToBufferedImage(Image a) {
		if (image instanceof BufferedImage) {
			return;
		}
		image = new ImageIcon(a).getImage();
		BufferedImage bufferedImage = new BufferedImage(image.getWidth(null), image.getHeight(null),  BufferedImage.TYPE_INT_ARGB);
		Graphics g = bufferedImage.createGraphics();
		g.drawImage(image, 0, 0, null);
		g.dispose();
		bImage = bufferedImage;
	}
	
    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event dispatch thread.
     */
    private static void createAndShowGUI() {
        // make window
        JFrame frame = new JFrame("ImageSplitter");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // add content
        frame.add(new ImageSplitter());
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                //Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE); 
                createAndShowGUI();
            }
        });
    }
}