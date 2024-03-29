// // Copyright (c) FIRST and other WPILib contributors.
// // Open Source Software; you can modify and/or share it under the terms of
// // the WPILib BSD license file in the root directory of this project.

// package frc.robot.commands;

// import frc.robot.Constants.Auton;
// import frc.robot.Constants.NoteHandlerSpeeds;
// import frc.robot.Constants.ShooterConstants;
// import frc.robot.subsystems.Intake;
// import frc.robot.subsystems.Shooter;

// import java.util.function.BooleanSupplier;
// import java.util.function.DoubleSupplier;

// import edu.wpi.first.math.geometry.Rotation2d;
// import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
// import edu.wpi.first.wpilibj2.command.Command;

// /** An example command that uses an example subsystem. */
// public class NoteHandlerCommand extends Command {
//   private final Shooter shooter;
//   private final Intake intake;
//   private final DoubleSupplier intakeSpeedAxis;
//   private final BooleanSupplier shooterButtonSupplier, ampAlignSupplier, ampScoreSupplier, hpStationLoadSupplier, ejectSupplier, unjamSupplier, climbSupplier;

//   private enum State {
//     SHOOTING, IDLE, HOLDING, SPOOLING, AUTO_SPOOLING, AUTO_SHOOTING, INDEXING_REV, INDEXING_FWD, AMP_ALIGNING,
//     AMP_SCORING_A, AMP_SCORING_B, HP_STATION_LOAD, CLIMB, UNJAM, EJECT_DOWNSPOOL, EJECT;
//   }

//   private State currentState;
//   private double intakeSpeed, portShootingSpeed, starboardShootingSpeed, loaderSpeed, commandedIntakeSpeed, portSpeed,
//       starboardSpeed;
//   private Rotation2d autoArmAngle, armAngle;
//   private boolean sensorvalue, shooterbutton, shooterAtSpeed, autoShooting, onTarget, armInPosition,
//     ampAligning, ampScoring, hpLoading, ejectButton, unjamButton, climbButton;

//   public NoteHandlerCommand(
//       Shooter shooter,
//       Intake intake,
//       DoubleSupplier intakeSpeedAxis,
//       BooleanSupplier shooterButtonSupplier,
//       BooleanSupplier ampAlignSupplier,
//       BooleanSupplier ampScoreSupplier,
//       BooleanSupplier playerStationLoadSupplier,
//       BooleanSupplier ejectSupplier,
//       BooleanSupplier unjamSupplier,
//       BooleanSupplier climbSupplier) {
//     this.shooter = shooter;
//     this.intake = intake;
//     this.intakeSpeedAxis = intakeSpeedAxis;
//     this.shooterButtonSupplier = shooterButtonSupplier;
//     this.ampAlignSupplier = ampAlignSupplier;
//     this.ampScoreSupplier = ampScoreSupplier;
//     this.hpStationLoadSupplier = playerStationLoadSupplier;
//     this.ejectSupplier = ejectSupplier;
//     this.unjamSupplier = unjamSupplier;
//     this.climbSupplier = climbSupplier;


//     // Use addRequirements() here to declare subsystem dependencies.
//     addRequirements(shooter, intake);
//   }

//   // Called when the command is initially scheduled.
//   @Override
//   public void initialize() {
//     currentState = State.IDLE;
//     portSpeed = NoteHandlerSpeeds.PORT_SHOOTER;
//     starboardSpeed = NoteHandlerSpeeds.STARBOARD_SHOOTER;

//     shooter.setShooterSpeed(NoteHandlerSpeeds.PORT_IDLE, NoteHandlerSpeeds.STARBOARD_IDLE);
//   }

//   // Called every time the scheduler runs while the command is scheduled.
//   @Override
//   public void execute() {
//     // Read inputs
//     sensorvalue = shooter.getNoteSensor();
//     shooterbutton = shooterButtonSupplier.getAsBoolean();
//     commandedIntakeSpeed = intakeSpeedAxis.getAsDouble() + SmartDashboard.getNumber(Auton.AUTO_INTAKE_SPEED_KEY, 0);
//     shooterAtSpeed = (Math.abs(portShootingSpeed - shooter.getPortShooterSpeed()) <= NoteHandlerSpeeds.SHOOTER_TOLERANCE)
//         &&
//         (Math.abs(starboardShootingSpeed - shooter.getStarboardShooterSpeed()) <= NoteHandlerSpeeds.SHOOTER_TOLERANCE);
//     armInPosition = shooter.armAtGoal();
//     portSpeed = SmartDashboard.getNumber("PortSpeed", portSpeed);
//     starboardSpeed = SmartDashboard.getNumber("StarboardSpeed", starboardSpeed);
//     autoShooting = SmartDashboard.getBoolean(Auton.AUTO_SHOOTING_KEY, false);
//     onTarget = SmartDashboard.getBoolean(Auton.ON_TARGET_KEY, false);
//     autoArmAngle = Rotation2d
//         .fromRadians(SmartDashboard.getNumber(Auton.ARM_ANGLE_KEY, ShooterConstants.ARM_LOWER_LIMIT.getRadians()));
//     ampAligning = ampAlignSupplier.getAsBoolean() || SmartDashboard.getBoolean(Auton.AUTO_AMP_ALIGN_KEY, false);
//     ampScoring = ampScoreSupplier.getAsBoolean() || SmartDashboard.getBoolean(Auton.AUTO_AMP_SCORE_KEY, false);
//     hpLoading = hpStationLoadSupplier.getAsBoolean();
//     ejectButton = ejectSupplier.getAsBoolean();
//     unjamButton = unjamSupplier.getAsBoolean();
//     climbButton = climbSupplier.getAsBoolean();

//     // Execute statemachine logic
//     switch (currentState) {
//       case IDLE:
//         // Set desired outputs in this state
//         intakeSpeed = commandedIntakeSpeed;
//         loaderSpeed = (commandedIntakeSpeed > 0) ? NoteHandlerSpeeds.LOADER_INTAKE
//             : NoteHandlerSpeeds.LOADER_IDLE;
//         portShootingSpeed = NoteHandlerSpeeds.PORT_IDLE;
//         starboardShootingSpeed = NoteHandlerSpeeds.STARBOARD_IDLE;
//         armAngle = ShooterConstants.ARM_LOWER_LIMIT;

//         // Determine if we neeed to change state
//         if (sensorvalue) {
//           currentState = State.INDEXING_REV;
//         }
//         if (hpLoading) {
//           currentState = State.HP_STATION_LOAD;
//         }
//         if (climbButton) {
//           currentState = State.CLIMB;
//         }
//         if (ampAligning) {
//           currentState = State.AMP_ALIGNING;
//         }
//         if (ampAligning && ampScoring) {
//           currentState = State.AMP_SCORING_A;
//         }
//         if (ejectButton) {
//           currentState = State.EJECT_DOWNSPOOL;
//         }
//         if (unjamButton) {
//           currentState = State.UNJAM;
//         }
//         if (shooterbutton) {
//           currentState = State.SPOOLING;
//         }
//         if (autoShooting) {
//           currentState = State.AUTO_SPOOLING;
//         }

//         // Don't forget this-- things get real weird when every successive case can
//         // execute
//         break;

//       case SPOOLING:
//         intakeSpeed = commandedIntakeSpeed < 0 ? commandedIntakeSpeed : NoteHandlerSpeeds.INTAKE_IDLE;
//         loaderSpeed = commandedIntakeSpeed < 0 ? -NoteHandlerSpeeds.LOADER_INTAKE
//             : NoteHandlerSpeeds.LOADER_IDLE;
//         portShootingSpeed = portSpeed;
//         starboardShootingSpeed = starboardSpeed;
//         armAngle = ShooterConstants.ARM_MANUAL_SHOT_ANGLE;

//         if (!shooterbutton) {
//           currentState = sensorvalue ? State.INDEXING_REV : State.IDLE;
//         }
//         if (shooterbutton && shooterAtSpeed && armInPosition) {
//           currentState = State.SHOOTING;
//         }

//         break;

//       case AUTO_SPOOLING:
//         intakeSpeed = commandedIntakeSpeed;
//         loaderSpeed = commandedIntakeSpeed < 0 ? -NoteHandlerSpeeds.LOADER_INTAKE
//             : NoteHandlerSpeeds.LOADER_IDLE;
//         portShootingSpeed = portSpeed;
//         starboardShootingSpeed = starboardSpeed;
//         armAngle = autoArmAngle;

//         if (!autoShooting) {
//           currentState = sensorvalue ? State.INDEXING_REV : State.IDLE;
//         }
//         if (autoShooting && shooterAtSpeed && armInPosition && onTarget) {
//           currentState = State.AUTO_SHOOTING;
//         }

//         break;

//       case SHOOTING:
//         intakeSpeed = commandedIntakeSpeed < 0 ? commandedIntakeSpeed : NoteHandlerSpeeds.INTAKE_IDLE;
//         loaderSpeed = NoteHandlerSpeeds.LOADER_FIRING;
//         portShootingSpeed = portSpeed;
//         starboardShootingSpeed = starboardSpeed;
//         armAngle = ShooterConstants.ARM_MANUAL_SHOT_ANGLE;

//         if (!shooterbutton) {
//           currentState = sensorvalue ? State.INDEXING_REV : State.IDLE;
//         }

//         break;

//       case AUTO_SHOOTING:
//         intakeSpeed = commandedIntakeSpeed;
//         loaderSpeed = NoteHandlerSpeeds.LOADER_FIRING;
//         portShootingSpeed = portSpeed;
//         starboardShootingSpeed = starboardSpeed;
//         armAngle = autoArmAngle;

//         if (!autoShooting) {
//           currentState = sensorvalue ? State.INDEXING_REV : State.IDLE;
//         }

//         break;

//       case INDEXING_REV:
//         intakeSpeed = commandedIntakeSpeed < 0 ? commandedIntakeSpeed : NoteHandlerSpeeds.INTAKE_IDLE;
//         loaderSpeed = -NoteHandlerSpeeds.LOADER_INDEXING;
//         portShootingSpeed = NoteHandlerSpeeds.PORT_IDLE;
//         starboardShootingSpeed = NoteHandlerSpeeds.STARBOARD_IDLE;
//         armAngle = ShooterConstants.ARM_LOWER_LIMIT;

//         if (!sensorvalue) {
//           currentState = State.INDEXING_FWD;
//         }
//         if (climbButton) {
//           currentState = State.CLIMB;
//         }
//         if (ampAligning) {
//           currentState = State.AMP_ALIGNING;
//         }
//         if (ampAligning && ampScoring) {
//           currentState = State.AMP_SCORING_A;
//         }
//         if (ejectButton) {
//           currentState = State.EJECT_DOWNSPOOL;
//         }
//         if (unjamButton) {
//           currentState = State.UNJAM;
//         }
//         if (shooterbutton) {
//           currentState = State.SPOOLING;
//         }
//         if (autoShooting) {
//           currentState = State.AUTO_SPOOLING;
//         }

//         break;

//       case INDEXING_FWD:
//         intakeSpeed = commandedIntakeSpeed < 0 ? commandedIntakeSpeed : NoteHandlerSpeeds.INTAKE_IDLE;
//         loaderSpeed = NoteHandlerSpeeds.LOADER_INDEXING;
//         portShootingSpeed = NoteHandlerSpeeds.PORT_IDLE;
//         starboardShootingSpeed = NoteHandlerSpeeds.STARBOARD_IDLE;
//         armAngle = ShooterConstants.ARM_LOWER_LIMIT;

//         if (sensorvalue) {
//           currentState = State.HOLDING;
//         }
//         if (climbButton) {
//           currentState = State.CLIMB;
//         }
//         if (ampAligning) {
//           currentState = State.AMP_ALIGNING;
//         }
//         if (ampAligning && ampScoring) {
//           currentState = State.AMP_SCORING_A;
//         }
//         if (ejectButton) {
//           currentState = State.EJECT_DOWNSPOOL;
//         }
//         if (unjamButton) {
//           currentState = State.UNJAM;
//         }
//         if (shooterbutton) {
//           currentState = State.SPOOLING;
//         }
//         if (autoShooting) {
//           currentState = State.AUTO_SPOOLING;
//         }

//         break;

//       case HOLDING:
//         intakeSpeed = commandedIntakeSpeed < 0 ? commandedIntakeSpeed : NoteHandlerSpeeds.INTAKE_IDLE;
//         loaderSpeed = commandedIntakeSpeed < 0 ? -NoteHandlerSpeeds.LOADER_INTAKE
//             : NoteHandlerSpeeds.LOADER_IDLE;
//         portShootingSpeed = portSpeed;
//         starboardShootingSpeed = starboardSpeed;
//         armAngle = ShooterConstants.ARM_LOWER_LIMIT;

//         if (climbButton) {
//           currentState = State.CLIMB;
//         }
//         if (ampAligning) {
//           currentState = State.AMP_ALIGNING;
//         }
//         if (ampAligning && ampScoring) {
//           currentState = State.AMP_SCORING_A;
//         }
//         if (ejectButton) {
//           currentState = State.EJECT_DOWNSPOOL;
//         }
//         if (unjamButton) {
//           currentState = State.UNJAM;
//         }
//         if (shooterbutton) {
//           currentState = State.SPOOLING;
//         }
//         if (autoShooting) {
//           currentState = State.AUTO_SPOOLING;
//         }

//         break;

//         case AMP_ALIGNING:
//           intakeSpeed = commandedIntakeSpeed < 0 ? commandedIntakeSpeed : NoteHandlerSpeeds.INTAKE_IDLE;
//           loaderSpeed = commandedIntakeSpeed < 0 ? -NoteHandlerSpeeds.LOADER_INTAKE
//               : NoteHandlerSpeeds.LOADER_IDLE;
//           portShootingSpeed = NoteHandlerSpeeds.PORT_AMP_SCORE;
//           starboardShootingSpeed = NoteHandlerSpeeds.STARBOARD_AMP_SCORE;
//           armAngle = ShooterConstants.ARM_AMP_ALIGNMENT_ANGLE;

//           if (!ampAligning) {
//             currentState = sensorvalue ? State.INDEXING_REV : State.IDLE;
//           }
//           if (shooterbutton) {
//             currentState = State.SPOOLING;
//           }
//           if (autoShooting) {
//             currentState = State.AUTO_SPOOLING;
//           }
//           if (ampAligning && ampScoring) {
//             currentState = State.AMP_SCORING_A;
//           }
        
//         break;

//         case AMP_SCORING_A:
//           intakeSpeed = commandedIntakeSpeed < 0 ? commandedIntakeSpeed : NoteHandlerSpeeds.INTAKE_IDLE;
//           loaderSpeed = commandedIntakeSpeed < 0 ? -NoteHandlerSpeeds.LOADER_INTAKE
//               : NoteHandlerSpeeds.LOADER_IDLE;
//           portShootingSpeed = NoteHandlerSpeeds.PORT_AMP_SCORE;
//           starboardShootingSpeed = NoteHandlerSpeeds.STARBOARD_AMP_SCORE;
//           armAngle = ShooterConstants.ARM_AMP_SCORE_ANGLE;

//           if (!ampScoring) {
//             currentState = State.AMP_ALIGNING;
//           }
//           if (!ampAligning) {
//             currentState = sensorvalue ? State.INDEXING_REV : State.IDLE;
//           }
//           if (shooterbutton) {
//             currentState = State.SPOOLING;
//           }
//           if (autoShooting) {
//             currentState = State.AUTO_SPOOLING;
//           }
//           if (ampScoring && ampAligning && armInPosition) {
//             currentState = State.AMP_SCORING_B;
//           }

//         break;
      
//         case AMP_SCORING_B:
//           intakeSpeed = commandedIntakeSpeed < 0 ? commandedIntakeSpeed : NoteHandlerSpeeds.INTAKE_IDLE;
//           loaderSpeed = NoteHandlerSpeeds.LOADER_FIRING;
//           portShootingSpeed = NoteHandlerSpeeds.PORT_AMP_SCORE;
//           starboardShootingSpeed = NoteHandlerSpeeds.STARBOARD_AMP_SCORE;
//           armAngle = ShooterConstants.ARM_AMP_SCORE_ANGLE;

//           if (!ampScoring) {
//             currentState = State.AMP_ALIGNING;
//           }
//           if (!ampAligning) {
//             currentState = sensorvalue ? State.INDEXING_REV : State.IDLE;
//           }
//           if (shooterbutton) {
//             currentState = State.SPOOLING;
//           }
//           if (autoShooting) {
//             currentState = State.AUTO_SPOOLING;
//           }
        
//         break;

//         case HP_STATION_LOAD:
//           intakeSpeed = NoteHandlerSpeeds.INTAKE_IDLE;
//           loaderSpeed = -NoteHandlerSpeeds.LOADER_FIRING;
//           portShootingSpeed = NoteHandlerSpeeds.SHOOTER_INTAKE;
//           starboardShootingSpeed = NoteHandlerSpeeds.SHOOTER_INTAKE;
//           armAngle = ShooterConstants.ARM_HP_COLLECT_ANGLE;

//           if (!hpLoading) {
//             currentState = sensorvalue ? State.INDEXING_REV : State.IDLE;
//           }
//           if (sensorvalue) {
//             currentState = State.INDEXING_REV;
//           }
//           if (shooterbutton) {
//             currentState = State.SPOOLING;
//           }
//           if (autoShooting) {
//             currentState = State.AUTO_SPOOLING;
//           }
//         break;
      
//         case EJECT_DOWNSPOOL:
//           intakeSpeed = commandedIntakeSpeed;
//           loaderSpeed = commandedIntakeSpeed < 0 ? -NoteHandlerSpeeds.LOADER_INTAKE
//               : NoteHandlerSpeeds.LOADER_IDLE;
//           portShootingSpeed = NoteHandlerSpeeds.PORT_EJECT;
//           starboardShootingSpeed = NoteHandlerSpeeds.STARBOARD_EJECT;
//           armAngle = ShooterConstants.ARM_LOWER_LIMIT;

//           if (shooterAtSpeed) {
//             currentState = State.EJECT;
//           }
//           if (!ejectButton) {
//             currentState = sensorvalue ? State.INDEXING_REV : State.IDLE;
//           }
//           if (autoShooting) {
//             currentState = State.AUTO_SPOOLING;
//           }
//         break;
      
//         case EJECT:
//             intakeSpeed = commandedIntakeSpeed;
//             loaderSpeed = NoteHandlerSpeeds.LOADER_FIRING;
//             portShootingSpeed = NoteHandlerSpeeds.PORT_EJECT;
//             starboardShootingSpeed = NoteHandlerSpeeds.STARBOARD_EJECT;
//             armAngle = ShooterConstants.ARM_LOWER_LIMIT;

//             if (!ejectButton) {
//               currentState = sensorvalue ? State.INDEXING_REV : State.IDLE;
//             }
//             if (autoShooting) {
//               currentState = State.AUTO_SPOOLING;
//             }
//           break;

//           case UNJAM:
//             intakeSpeed = NoteHandlerSpeeds.INTAKE_UNJAM;
//             loaderSpeed = NoteHandlerSpeeds.LOADER_INTAKE;
//             portShootingSpeed = NoteHandlerSpeeds.PORT_IDLE;
//             starboardShootingSpeed = NoteHandlerSpeeds.STARBOARD_IDLE;
//             armAngle = ShooterConstants.ARM_LOWER_LIMIT;

//             if (!unjamButton) {
//               currentState = sensorvalue ? State.INDEXING_REV : State.IDLE;
//             }
//             if (sensorvalue) {
//               currentState = State.INDEXING_REV;
//             }
//             if (ejectButton) {
//               currentState = State.EJECT_DOWNSPOOL;
//             }
//             if (autoShooting) {
//               currentState = State.AUTO_SPOOLING;
//             }
//           break;

//           case CLIMB:
//             intakeSpeed = commandedIntakeSpeed;
//             loaderSpeed = commandedIntakeSpeed < 0 ? -NoteHandlerSpeeds.LOADER_INTAKE : NoteHandlerSpeeds.LOADER_IDLE;
//             portShootingSpeed = NoteHandlerSpeeds.PORT_CLIMB;
//             starboardShootingSpeed = NoteHandlerSpeeds.STARBOARD_CLIMB;
//             armAngle = ShooterConstants.ARM_CLIMB_ANGLE;

//             if (!climbButton) {
//               currentState = sensorvalue ? State.INDEXING_REV : State.IDLE;
//             }
//             if (autoShooting) {
//               currentState = State.AUTO_SPOOLING;
//             }
//           break;
//     }

//     // Apply outputs
//     intake.runRollers(intakeSpeed);
//     shooter.setShooterSpeed(portShootingSpeed, starboardShootingSpeed);
//     shooter.runLoader(loaderSpeed);
//     shooter.setArmAngle(armAngle);

//     SmartDashboard.putString("currentState", currentState.toString());
//     SmartDashboard.putNumber("intakeSpeed", intakeSpeed);
//     SmartDashboard.putNumber("shooterSpeed", portShootingSpeed);
//     SmartDashboard.putNumber("loaderSpeed", loaderSpeed);
//     SmartDashboard.putBoolean("sensor", sensorvalue);
//   }

//   // Called once the command ends or is interrupted.
//   @Override
//   public void end(boolean interrupted) {
//   }

//   // Returns true when the command should end.
//   @Override
//   public boolean isFinished() {
//     return false;
//   }
// }
