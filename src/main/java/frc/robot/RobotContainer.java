// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

// import frc.robot.Constants.Auton;
import frc.robot.Constants.Drivebase;
import frc.robot.Constants.OperatorConstants;
import frc.robot.commands.AutoShoot;
// import frc.robot.commands.AutoAmp;
// import frc.robot.commands.AutoShoot;
// import frc.robot.commands.Autos;
import frc.robot.drivebase.AbsoluteDrive;
// import frc.robot.commands.NoteHandlerCommand;
// import frc.robot.subsystems.Climber;
// import frc.robot.drivebase.TeleopDrive;
// import frc.robot.subsystems.Intake;
// import frc.robot.subsystems.Shooter;
import frc.robot.subsystems.SwerveBase;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.choreo.lib.Choreo;
import com.choreo.lib.ChoreoTrajectory;

import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
// import edu.wpi.first.wpilibj2.command.button.CommandJoystick;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
//import edu.wpi.first.wpilibj2.command.sysid.SysIdRoutine;

/**
 * This class is where the bulk of the robot should be declared. Since
 * Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in
 * the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of
 * the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer {
  private final Map<String, ChoreoTrajectory> trajMap;

  // The robot's subsystems and commands are defined here...
  // private final Intake intake = new Intake();
  private final SwerveBase drivebase = new SwerveBase();
  // private final Shooter shooter = new Shooter();
  // private final Climber climber = new Climber();
  // private final TeleopDrive openRobotRel, closedRobotRel, openFieldRel, closedFieldRel;
  private final AbsoluteDrive absoluteDrive;

  private final CommandXboxController driveController = new CommandXboxController(OperatorConstants.driveControllerPort);
  // private final CommandJoystick driveController = new CommandJoystick(OperatorConstants.driveControllerPort);
  // private final CommandJoystick headingController = new CommandJoystick(OperatorConstants.headingControllerPort);
  private final CommandXboxController operatorController = new CommandXboxController(OperatorConstants.operatorControllerPort);
  
  private Command autoCommand;
  SendableChooser<Command> chooser = new SendableChooser<>();

  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   */
  public RobotContainer() {

    /*openRobotRel = new TeleopDrive(
        drivebase,
        () -> (Math.abs(driveController.getY()) > OperatorConstants.joystickDeadband)
            ? -driveController.getY()
            : 0,
        () -> (Math.abs(driveController.getX()) > OperatorConstants.joystickDeadband)
            ? -driveController.getX()
            : 0,
        () -> -headingController.getTwist(), () -> false, true);

    closedRobotRel = new TeleopDrive(
        drivebase,
        () -> (Math.abs(driveController.getY()) > OperatorConstants.joystickDeadband)
            ? -driveController.getY()
            : 0,
        () -> (Math.abs(driveController.getX()) > OperatorConstants.joystickDeadband)
            ? -driveController.getX()
            : 0,
        () -> -headingController.getTwist(), () -> false, false);

    openFieldRel = new TeleopDrive(
        drivebase,
        () -> (Math.abs(driveController.getY()) > OperatorConstants.joystickDeadband)
            ? driveController.getY()
            : 0,
        () -> (Math.abs(driveController.getX()) > OperatorConstants.joystickDeadband)
            ? driveController.getX()
            : 0,
        () -> -headingController.getTwist(), () -> true, true);

    closedFieldRel = new TeleopDrive(
        drivebase,
        () -> (Math.abs(driveController.getY()) > OperatorConstants.joystickDeadband)
            ? -driveController.getY()
            : 0,
        () -> (Math.abs(driveController.getX()) > OperatorConstants.joystickDeadband)
            ? -driveController.getX()
            : 0,
        () -> -headingController.getTwist(), () -> true, false);*/

    absoluteDrive = new AbsoluteDrive(
        drivebase,
        () -> (Math.abs(driveController.getLeftY()) > OperatorConstants.joystickDeadband)
            ? -driveController.getLeftY()
            : 0,
        () -> (Math.abs(driveController.getLeftX()) > OperatorConstants.joystickDeadband)
            ? -driveController.getLeftX()
            : 0,
        () -> -driveController.getRightX(),
        () -> -driveController.getRightY(),
        false,
        () -> driveController.a().getAsBoolean());

    // NoteHandlerCommand noteManager = new NoteHandlerCommand(
    //     shooter,
    //     intake,
    //     () -> (operatorController.getLeftTriggerAxis() - operatorController.getRightTriggerAxis()),
    //     () -> operatorController.y().getAsBoolean(),
    //     () -> operatorController.leftBumper().getAsBoolean(),
    //     () -> operatorController.rightBumper().getAsBoolean(),
    //     () -> headingController.button(2).getAsBoolean(),
    //     () -> operatorController.b().getAsBoolean(),
    //     () -> operatorController.a().getAsBoolean(),
    //     () -> operatorController.x().getAsBoolean());

    // shooter.setDefaultCommand(noteManager);

    drivebase.setDefaultCommand(absoluteDrive);
    // Configure the trigger bindings
    configureBindings();

    SmartDashboard.putString("Selected Auto:", "NONE!!!");

    trajMap = loadTrajectories();

    // Command oneNoteAuto = Autos.shootPreload(drivebase);
    // Command centerAuto = Autos.twoNoteCenter(drivebase, intake);
    // Command centerAmpAuto = Autos.threeNoteCenterAmp(drivebase, intake, trajMap);
    // Command fourNoteAuto = Autos.fourNearNotes(drivebase, intake, trajMap);
    // Command podiumAuto = Autos.twoNotePodium(drivebase, intake);
    // Command ampAuto = Autos.twoNoteAmp(drivebase, intake);
    // Command ampCenterAuto = Autos.threeNoteAmpCenter(drivebase, intake, trajMap);
    // Command centerPodium = Autos.threeNoteCenterPodium(drivebase, intake, trajMap);
    // chooser.addOption(oneNoteAuto.getName(), oneNoteAuto);
    // chooser.addOption(centerAuto.getName(), centerAuto);
    // chooser.addOption(centerAmpAuto.getName(), centerAmpAuto);
    // chooser.addOption(podiumAuto.getName(), podiumAuto);
    // chooser.addOption(ampAuto.getName(), ampAuto);
    // chooser.addOption(ampCenterAuto.getName(), ampCenterAuto);
    // chooser.addOption(centerPodium.getName(), centerPodium);
    // chooser.setDefaultOption(fourNoteAuto.getName(), fourNoteAuto);
    // SmartDashboard.putData(chooser);
    // SmartDashboard.putData("SetAuto", new InstantCommand(() -> {
    //   autoCommand = chooser.getSelected();
    //   SmartDashboard.putString("Selected Auto:", autoCommand.getName());
    // }).ignoringDisable(true));

    SmartDashboard.putData("setGains", new InstantCommand(drivebase::setVelocityModuleGains));
    SmartDashboard.putData("SendAlliance",
      new InstantCommand(
        () -> drivebase.setAlliance(DriverStation.getAlliance().get())
      )
      .ignoringDisable(true)
    );

    SmartDashboard.putNumber("KV", Drivebase.KV);
    SmartDashboard.putNumber("KA", Drivebase.KA);
    SmartDashboard.putNumber("KP", 0);
    SmartDashboard.putNumber("KI", 0);
    SmartDashboard.putNumber("KD", 0);
  }

  /**
   * Use this method to define your trigger->command mappings. Triggers can be
   * created via the
   * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with
   * an arbitrary
   * predicate, or via the named factories in {@link
   * edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for
   * {@link
   * CommandXboxController
   * Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller
   * PS4} controllers or
   * {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight
   * joysticks}.
   */
  private void configureBindings() {
    /*
     * operatorController.y().whileTrue(shooter.sysIdQuasiShoulder(SysIdRoutine.
     * Direction.kForward));
     * operatorController.b().whileTrue(shooter.sysIdQuasiShoulder(SysIdRoutine.
     * Direction.kReverse));
     * operatorController.a().whileTrue(shooter.sysIdDynShoulder(SysIdRoutine.
     * Direction.kForward));
     * operatorController.x().whileTrue(shooter.sysIdDynShoulder(SysIdRoutine.
     * Direction.kReverse));
     */
    driveController.button(1).whileTrue(new AutoShoot(drivebase));
    driveController.button(8).onTrue(new InstantCommand(drivebase::clearOdometrySeed).ignoringDisable(true));
    operatorController.start().onTrue(new InstantCommand(drivebase::clearOdometrySeed).ignoringDisable(true));
    /*driveController.button(2).whileTrue(new AutoAmp(drivebase)).onFalse(new InstantCommand(() -> {
      SmartDashboard.putBoolean(Auton.AUTO_AMP_SCORE_KEY, false);
      SmartDashboard.putBoolean(Auton.AUTO_AMP_ALIGN_KEY, false);
    }));*/
    // headingController.button(1).onTrue(new InstantCommand(() -> SmartDashboard.putNumber(Auton.AUTO_INTAKE_SPEED_KEY, 1)))
    // .onFalse(new InstantCommand(() -> SmartDashboard.putNumber(Auton.AUTO_INTAKE_SPEED_KEY, 0)));
    // operatorController.povDown().onTrue(climber.runWinches(-0.7));
    // operatorController.povUp().onTrue(climber.runWinches(0.7));
    // operatorController.povCenter().onTrue(climber.runWinches(0));
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  // public Command getAutonomousCommand() {
  //   // An example command will be run in autonomous
  //   return Autos.fourNearNotes(drivebase, intake, trajMap);
  // }

  public void stopDrive() {
    drivebase.setChassisSpeeds(new ChassisSpeeds());
  }

  public void sendAlliance() {
    drivebase.setAlliance(DriverStation.getAlliance().get());
  }

  private Map<String, ChoreoTrajectory> loadTrajectories() {
    Set<String> trajNames;
    try {
      if (Robot.isReal()) {
        trajNames = listFilesUsingFilesList("/home/lvuser/deploy/choreo");
      } else {
        trajNames = listFilesUsingFilesList("src/main/deploy/choreo");
      }
    } catch (IOException e) {
      DriverStation.reportError("Invalid Directory! Trajectories failed to load!", true);
      return null;
    }
    return trajNames.stream().collect(Collectors.toMap(
        entry -> entry.replace(".traj", ""),
        entry -> Choreo.getTrajectory(entry.replace(".traj", ""))));
  }

  private Set<String> listFilesUsingFilesList(String dir) throws IOException {
    try (Stream<Path> stream = Files.list(Paths.get(dir))) {
      return stream
          .filter(file -> !Files.isDirectory(file))
          .map(Path::getFileName)
          .map(Path::toString)
          .collect(Collectors.toSet());
    }
  }
}