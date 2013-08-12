package nchadoop;

import java.io.IOException;
import java.net.URI;

import lombok.Data;
import nchadoop.fs.Directory;
import nchadoop.fs.HdfsScanner;
import nchadoop.fs.SearchRoot;
import nchadoop.ui.MainWindow;
import nchadoop.ui.ScanningPopup;

import org.apache.hadoop.fs.LocatedFileStatus;

import com.googlecode.lanterna.gui.GUIScreen;
import com.googlecode.lanterna.gui.Window;
import com.googlecode.lanterna.gui.dialog.MessageBox;
import com.googlecode.lanterna.input.Key;
import com.googlecode.lanterna.input.Key.Kind;

@Data
public class Controller
{
	private GUIScreen		guiScreen;
	private MainWindow		mainWindow;
	private ScanningPopup	scanningPopup;
	private HdfsScanner		hdfsScanner;

	public void startScan(final URI uri)
	{
		this.mainWindow.init();

		try
		{
			final SearchRoot searchRoot = hdfsScanner.refresh(uri, scanningPopup);

			this.mainWindow.updateSearchRoot(searchRoot);
		}
		catch (final Exception e)
		{
			scanningPopup.close();
			MessageBox.showMessageBox(guiScreen, "Error", "Error: " + e);
			shutdown();
		}
	}

	public void shutdown()
	{
		scanningPopup.close();
		mainWindow.close();
		guiScreen.getScreen().stopScreen();
		hdfsScanner.close();
	}

	public boolean handleGlobalKeyPressed(Window sender, Key key)
	{
		if (key.getCharacter() == 'q' || key.getKind() == Kind.Escape)
		{
			shutdown();
			return true;
		}

		return false;
	}

	public void deleteDiretory(Directory directory)
	{
		if (directory.isRoot())
		{
			MessageBox.showMessageBox(guiScreen, "Error", "Couldn't the search root.");
			return;
		}

		try
		{
			if (!hdfsScanner.deleteDirectory(directory))
			{
				MessageBox.showMessageBox(guiScreen, "Error", "Couldn't delete this.");
			}
			else
			{
				mainWindow.changeFolder(directory.getParent());
			}
		}
		catch (IOException e)
		{
			MessageBox.showMessageBox(guiScreen, "Error", "Error: " + e.getMessage());
		}
	}

	public void deleteFile(Directory parent, LocatedFileStatus file)
	{
		try
		{
			if (hdfsScanner.deleteFile(parent, file) == false)
			{
				throw new IOException("Couldn't delete this file");
			}
			else
			{
				mainWindow.changeFolder(parent);
			}
		}
		catch (IOException e)
		{
			MessageBox.showMessageBox(guiScreen, "Error", "Error: " + e.getMessage());
		}
	}
}
