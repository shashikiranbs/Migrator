package com.framework.migrator.main;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.framework.migrator.util.FileUtil;

public class FrameworkMigrator {
	private JFrame mainFrame;
	private JLabel headerLabel;
	private JLabel statusLabel;
	private JPanel controlPanel;

	public FrameworkMigrator() {
		prepareGUI();
	}

	private void prepareGUI() {
		mainFrame = new JFrame("Framework Migrator");
		mainFrame.setSize(700, 600);
		mainFrame.setLayout(new GridLayout(3, 1));
		mainFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent windowEvent) {
				System.exit(0);
			}
		});
		headerLabel = new JLabel("", JLabel.CENTER);
		statusLabel = new JLabel("", JLabel.CENTER);

		statusLabel.setSize(450, 200);

		controlPanel = new JPanel();
		controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

		mainFrame.add(headerLabel);
		mainFrame.add(controlPanel);
		mainFrame.add(statusLabel);
		mainFrame.setVisible(true);
	}

	private void displayGUI() {
		headerLabel.setText("Migrator Inputs");

		JLabel oldProjectPathLabel = new JLabel(
				"Input Project(Struts) Directory Path", JLabel.RIGHT);
		JLabel newProjectPathLabel = new JLabel(
				"Output Project(Spring) Directory Path", JLabel.RIGHT);
		JLabel newProjectNameLabel = new JLabel("New Project(Spring) Name",
				JLabel.RIGHT);
		final JTextField oldProjectPathField = new JTextField(10);
		final JTextField newProjectPathField = new JTextField(10);
		final JTextField newProjectNameField = new JTextField(10);

		JButton btnBrowse = new JButton("Browse");
		btnBrowse.setBounds(10, 41, 87, 23);

		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				// For Directory
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				// For File
				// fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				fileChooser.setAcceptAllFileFilterUsed(false);
				int rVal = fileChooser.showOpenDialog(null);
				if (rVal == JFileChooser.APPROVE_OPTION) {
					oldProjectPathField.setText(fileChooser.getSelectedFile()
							.toString());
				}
			}
		});

		JButton migrateBtn = new JButton("Migrate");
		migrateBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String path = newProjectPathField.getText();
				String name = newProjectNameField.getText();
				String inputPath = oldProjectPathField.getText();
				if (StringUtils.isNotBlank(path)
						&& StringUtils.isNotBlank("name")
						&& StringUtils.isNotBlank("inputPath")) {
					process(path, name, inputPath);
				}
			}
		});

		controlPanel.add(oldProjectPathLabel);
		controlPanel.add(oldProjectPathField);
		controlPanel.add(btnBrowse);
		controlPanel.add(newProjectNameLabel);
		controlPanel.add(newProjectNameField);
		controlPanel.add(newProjectPathLabel);
		controlPanel.add(newProjectPathField);
		controlPanel.add(migrateBtn);
		mainFrame.setVisible(true);
	}

	public void process(String path, String projectName, String inputAppPath) {
		/*
		 * String path = "/Users/shashikiranbs/Downloads/"; String projectName =
		 * "SampleSpringApp";
		 */
		String outputAppPath = path + projectName;

		// String inputAppPath =
		// "/Users/shashikiranbs/Documents/workspace/StrutsLoginApp";

		// Step 1: initialize project structure and spring basic libraries

		// 1.1 Create project folder @ given path

		try {

			File mainDir = new File(outputAppPath);
			if (!mainDir.exists()) {
				if (mainDir.mkdir()) {
					System.out.println("Directory is created!");
				} else {
					System.out.println("Failed to create directory!");
				}
			}
			// 1.2 Initial project data structure
			String[] initialDirs = { "src", "build/classes",
					"WebContent/META-INF", "WebContent/WEB-INF/lib" };
			// 1.3 Creating basic project folders
			for (String dir : initialDirs) {
				File subDirs = new File(outputAppPath + "/" + dir);
				if (mainDir.exists()) {
					if (subDirs.mkdirs()) {
						System.out.println("Multiple directories are created!");
					} else {
						System.out
								.println("Failed to create multiple directories!");
					}
				}
			}
			// 1.4 copying spring related libraries
			String sourcePath = "./config/spring/lib";
			String destinationPath = outputAppPath + "/WebContent/WEB-INF/lib";

			FileUtils.copyDirectory(new File(sourcePath), new File(
					destinationPath));

			// Step 2: Starts analyzing input project

			File[] projectDirs = new File(inputAppPath)
					.listFiles(new FileFilter() {
						@Override
						public boolean accept(File file) {
							return file.isDirectory()
									&& (file.getName().equalsIgnoreCase("src") || file
											.getName().equalsIgnoreCase(
													"WebContent"));
						}
					});

			if (projectDirs.length == 0) {
				System.out.println("Empty project input for migrator");
			} else {
				for (File dir : projectDirs) {
					if (dir.getName().equalsIgnoreCase("src")) {

						for (File subDir : dir.listFiles()) {
							if (subDir.isDirectory()) {
								List<File> subDirs = FileUtil
										.getSubdirs(subDir);
								for (File folder : subDirs) {
									if (folder.listFiles().length != 0) {
										System.out.println("dirToProcess:"
												+ folder.getCanonicalPath());
										new SourceMigrateAction()
												.processSourceFiles(folder,
														inputAppPath,
														outputAppPath);

									}
								}
							}
						}

					} else if (dir.getName().equalsIgnoreCase("WebContent")) {
						System.out.println("dirToProcess:"
								+ dir.getCanonicalPath());
						new SourceMigrateAction().processWebContent(dir,
								inputAppPath, outputAppPath);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {

		FrameworkMigrator fm = new FrameworkMigrator();
		fm.displayGUI();
	}
}
