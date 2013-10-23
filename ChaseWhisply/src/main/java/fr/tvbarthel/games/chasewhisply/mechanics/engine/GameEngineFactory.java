package fr.tvbarthel.games.chasewhisply.mechanics.engine;


import android.content.Context;

import fr.tvbarthel.games.chasewhisply.mechanics.behaviors.GameBehaviorFactory;
import fr.tvbarthel.games.chasewhisply.mechanics.behaviors.GameBehaviorTime;
import fr.tvbarthel.games.chasewhisply.mechanics.behaviors.GameBehaviorTutorial;
import fr.tvbarthel.games.chasewhisply.mechanics.informations.GameInformation;
import fr.tvbarthel.games.chasewhisply.mechanics.informations.GameInformationTime;
import fr.tvbarthel.games.chasewhisply.mechanics.informations.GameInformationTutorial;
import fr.tvbarthel.games.chasewhisply.mechanics.routine.Routine;
import fr.tvbarthel.games.chasewhisply.mechanics.routine.RoutineTicker;
import fr.tvbarthel.games.chasewhisply.model.DisplayableItemFactory;
import fr.tvbarthel.games.chasewhisply.model.GameMode;
import fr.tvbarthel.games.chasewhisply.model.GameModeFactory;
import fr.tvbarthel.games.chasewhisply.model.TargetableItem;
import fr.tvbarthel.games.chasewhisply.model.weapon.Weapon;
import fr.tvbarthel.games.chasewhisply.model.weapon.WeaponFactory;
import fr.tvbarthel.games.chasewhisply.ui.gameviews.GameViewTime;
import fr.tvbarthel.games.chasewhisply.ui.gameviews.GameViewTimeDecreasing;
import fr.tvbarthel.games.chasewhisply.ui.gameviews.GameViewTutorial;

public class GameEngineFactory {
	private static final long DEFAULT_SPAWNING_TIME = 1000;
	private static final long DEFAULT_TICKING_TIME = 1000;
	private static final long DEFAULT_STARTING_TIME = 30000;

	public static GameEngine create(final Context context, GameEngine.IGameEngine iGameEngine, GameMode gameMode) {
		GameEngine gameEngine = null;
		switch (gameMode.getType()) {
			case GameModeFactory.GAME_TYPE_REMAINING_TIME:
				gameEngine = createSprintOrMarathon(context, gameMode, iGameEngine);
				break;

			case GameModeFactory.GAME_TYPE_DEATH_TO_THE_KING:
				gameEngine = createDeathToTheKing(context, gameMode, iGameEngine);
				break;

			case GameModeFactory.GAME_TYPE_SURVIVAL:
				gameEngine = createSurvival(context, gameMode, iGameEngine);
				break;

			case GameModeFactory.GAME_TYPE_TUTORIAL:
				gameEngine = createTutorial(context, gameMode, iGameEngine);
				break;

		}
		return gameEngine;
	}

	public static GameEngine restore(final Context context, GameEngine.IGameEngine iGameEngine, GameInformation gameInformation) {
		GameEngine gameEngine = null;
		final int gameModeType = gameInformation.getGameMode().getType();
		switch (gameModeType) {
			case GameModeFactory.GAME_TYPE_REMAINING_TIME:
				gameEngine = createSprintOrMarathon(context, iGameEngine, (GameInformationTime) gameInformation);
				break;

			case GameModeFactory.GAME_TYPE_DEATH_TO_THE_KING:
				gameEngine = createDeathToTheKing(context, iGameEngine, (GameInformationTime) gameInformation);
				break;

			case GameModeFactory.GAME_TYPE_SURVIVAL:
				gameEngine = createSurvival(context, iGameEngine, (GameInformationTime) gameInformation);
				break;

			case GameModeFactory.GAME_TYPE_TUTORIAL:
				gameEngine = createTutorial(context, iGameEngine, (GameInformationTutorial) gameInformation);
				break;

		}
		return gameEngine;
	}

	private static GameEngine createTutorial(final Context context, final GameMode gameMode
			, final GameEngine.IGameEngine iGameEngine) {
		//Weapon
		final Weapon weapon = WeaponFactory.createBasicWeapon();

		//Game Information
		final GameInformationTutorial gameInformation = new GameInformationTutorial(gameMode, weapon);

		return createTutorial(context, iGameEngine, gameInformation);
	}


	private static GameEngine createTutorial(final Context context,
											 final GameEngine.IGameEngine iGameEngine,
											 final GameInformationTutorial gameInformation) {
		//Game Behavior
		final GameBehaviorTutorial gameBehavior = GameBehaviorFactory.createTutorial();
		gameBehavior.setGameInformation(gameInformation);

		//Game Engine & Game Behavior
		final GameEngineTutorial gameEngine = new GameEngineTutorial(context, iGameEngine, gameBehavior) {
			@Override
			public void onRun(int routineType, Object obj) {
				switch (routineType) {
					case Routine.TYPE_RELOADER:
						mGameBehavior.reload();
						break;
				}
			}
		};
		gameEngine.addRoutine(new Routine(Routine.TYPE_RELOADER, gameInformation.getWeapon().getReloadingTime()));
		gameBehavior.setInterface(gameEngine);

		//Game View
		final GameViewTutorial gameView = new GameViewTutorial(context, gameEngine);
		gameEngine.setGameView(gameView);

		return gameEngine;
	}

	private static GameEngine createDeathToTheKing(final Context context, final GameMode gameMode
			, final GameEngine.IGameEngine iGameEngine) {
		//Weapon
		final Weapon weapon = WeaponFactory.createBasicWeapon();

		//Game Information
		final GameInformationTime gameInformation = new GameInformationTime(gameMode,
				weapon, 1000);
		for (int i = 0; i < 100; i++) {
			if (i != 50) {
				gameInformation.addTargetableItem(DisplayableItemFactory.createGhostWithRandomCoordinates(
						TargetableItem.randomGhostTypeWithoutKing()));
			} else {
				gameInformation.addTargetableItem(DisplayableItemFactory.createKingGhostForDeathToTheKing());
			}

		}

		return createDeathToTheKing(context, iGameEngine, gameInformation);
	}

	private static GameEngine createDeathToTheKing(final Context context,
												   final GameEngine.IGameEngine iGameEngine,
												   GameInformationTime gameInformation) {
		//Game Behavior
		final GameBehaviorTime gameBehavior = GameBehaviorFactory.createDeathToTheKing();
		gameBehavior.setGameInformation(gameInformation);

		//Game Engine & Game Behavior
		final GameEngineTime gameEngine = new GameEngineTime(context, iGameEngine, gameBehavior) {
			@Override
			public void onRun(int routineType, Object obj) {
				switch (routineType) {
					case Routine.TYPE_RELOADER:
						mGameBehavior.reload();
						break;
					case Routine.TYPE_TICKER:
						mGameBehavior.tick((Long) obj);
						break;

				}
			}
		};
		gameEngine.addRoutine(new Routine(Routine.TYPE_RELOADER, gameInformation.getWeapon().getReloadingTime()));
		gameEngine.addRoutine(new RoutineTicker(DEFAULT_TICKING_TIME));
		gameBehavior.setInterface(gameEngine);

		//Game View
		final GameViewTime gameView = new GameViewTime(context, gameEngine);
		gameEngine.setGameView(gameView);

		return gameEngine;
	}

	private static GameEngine createSurvival(final Context context, final GameMode gameMode
			, final GameEngine.IGameEngine iGameEngine) {
		//Weapon
		final Weapon weapon = WeaponFactory.createBasicWeapon();

		//Game Information
		final GameInformationTime gameInformation = new GameInformationTime(gameMode,
				weapon, DEFAULT_STARTING_TIME);

		return createSurvival(context, iGameEngine, gameInformation);
	}


	private static GameEngine createSurvival(final Context context,
											 final GameEngine.IGameEngine iGameEngine,
											 GameInformationTime gameInformation) {
		//Game Behavior
		final GameBehaviorTime gameBehavior = GameBehaviorFactory.createSurvival();
		gameBehavior.setGameInformation(gameInformation);

		//Game Engine & Game Behavior
		final GameEngineTime gameEngine = new GameEngineTime(context, iGameEngine, gameBehavior) {
			@Override
			public void onRun(int routineType, Object obj) {
				switch (routineType) {
					case Routine.TYPE_RELOADER:
						mGameBehavior.reload();
						break;
					case Routine.TYPE_SPAWNER:
						//TODO
						mGameBehavior.spawn(50, 50);
						break;
					case Routine.TYPE_TICKER:
						mGameBehavior.tick((Long) obj);
						break;

				}
			}
		};
		gameEngine.addRoutine(new Routine(Routine.TYPE_RELOADER, gameInformation.getWeapon().getReloadingTime()));
		gameEngine.addRoutine(new Routine(Routine.TYPE_SPAWNER, DEFAULT_SPAWNING_TIME));
		gameEngine.addRoutine(new RoutineTicker(DEFAULT_TICKING_TIME));
		gameBehavior.setInterface(gameEngine);

		//Game View
		final GameViewTime gameView = new GameViewTimeDecreasing(context, gameEngine);
		gameEngine.setGameView(gameView);

		return gameEngine;
	}

	private static GameEngine createSprintOrMarathon(final Context context, final GameMode gameMode
			, final GameEngine.IGameEngine iGameEngine) {
		//Weapon
		final Weapon weapon = WeaponFactory.createBasicWeapon();

		//Game Information
		final GameInformationTime gameInformation = new GameInformationTime(gameMode,
				weapon, DEFAULT_STARTING_TIME * gameMode.getLevel());

		return createSprintOrMarathon(context, iGameEngine, gameInformation);
	}

	private static GameEngine createSprintOrMarathon(final Context context,
													 final GameEngine.IGameEngine iGameEngine,
													 GameInformationTime gameInformation) {
		//Game Behavior
		final GameBehaviorTime gameBehavior = GameBehaviorFactory.createSprintOrMarathon();
		gameBehavior.setGameInformation(gameInformation);

		//Game Engine & Game Behavior
		final GameEngineTime gameEngine = new GameEngineTime(context, iGameEngine, gameBehavior) {
			@Override
			public void onRun(int routineType, Object obj) {
				switch (routineType) {
					case Routine.TYPE_RELOADER:
						mGameBehavior.reload();
						break;
					case Routine.TYPE_SPAWNER:
						//TODO
						mGameBehavior.spawn(50, 50);
						break;
					case Routine.TYPE_TICKER:
						mGameBehavior.tick((Long) obj);
						break;

				}
			}
		};
		gameEngine.addRoutine(new Routine(Routine.TYPE_RELOADER, gameInformation.getWeapon().getReloadingTime()));
		gameEngine.addRoutine(new Routine(Routine.TYPE_SPAWNER, DEFAULT_SPAWNING_TIME));
		gameEngine.addRoutine(new RoutineTicker(DEFAULT_TICKING_TIME));
		gameBehavior.setInterface(gameEngine);

		//Game View
		final GameViewTime gameView = new GameViewTimeDecreasing(context, gameEngine);
		gameEngine.setGameView(gameView);

		return gameEngine;
	}
}