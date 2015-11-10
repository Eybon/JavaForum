package io.github.cr3ahal0.forum.client.impl;

import io.github.cr3ahal0.forum.client.ihm.Launcher;

public class Main {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new Thread() {
			@Override
			public void run() {
				javafx.application.Application.launch(Launcher.class);
			}
		}.start();
	}
}