package server.ListGame;

import model.GameData;

import java.util.ArrayList;

public record ListGameResponse (ArrayList<GameData> games){ }
