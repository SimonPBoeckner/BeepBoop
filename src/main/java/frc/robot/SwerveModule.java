package frc.robot;

import com.ctre.phoenix6.configs.MagnetSensorConfigs;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.signals.AbsoluteSensorRangeValue;
import com.ctre.phoenix6.signals.SensorDirectionValue;
// import com.revrobotics.AbsoluteEncoder;
import com.revrobotics.CANSparkFlex;
import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkPIDController;
import com.revrobotics.CANSparkBase.ControlType;
import com.revrobotics.CANSparkLowLevel.MotorType;
// import com.revrobotics.SparkAbsoluteEncoder.Type;

import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.lib.util.SwerveModuleConstants;
import frc.robot.Constants.Drivebase;

public class SwerveModule {
    public final int moduleNumber;
    private final double angleOffset;
    private final CANSparkMax angleMotor;
    private final CANSparkFlex driveMotor;
    private final CANcoder encoder;
    // private final AbsoluteEncoder absoluteEncoder;
    private final RelativeEncoder driveEncoder;
    private final SparkPIDController angleController, driveController;
    private final Timer time;
    private final Translation2d moduleLocation;
    private double angle, lastAngle, speed, fakePos, lastTime, dt;
    private SwerveModuleState desiredState = new SwerveModuleState();

    private SimpleMotorFeedforward feedforward;

    public SwerveModule(SwerveModuleConstants moduleConstants) {
        angle = 0;
        speed = 0;
        fakePos = 0;
        moduleNumber = moduleConstants.moduleNumber;
        angleOffset = moduleConstants.angleOffset;
        moduleLocation = new Translation2d(moduleConstants.xPos, moduleConstants.yPos);

        angleMotor = new CANSparkMax(moduleConstants.angleMotorID, MotorType.kBrushless);
        driveMotor = new CANSparkFlex(moduleConstants.driveMotorID, MotorType.kBrushless);
        angleMotor.restoreFactoryDefaults();
        driveMotor.restoreFactoryDefaults();

        //Config CANcoder encoders
        encoder = new CANcoder(moduleConstants.cancoderID);
        var config = new MagnetSensorConfigs();
        config.SensorDirection = Drivebase.ABSOLUTE_ENCODER_INVERT ? SensorDirectionValue.CounterClockwise_Positive : SensorDirectionValue.Clockwise_Positive;
        config.AbsoluteSensorRange = AbsoluteSensorRangeValue.Signed_PlusMinusHalf;
        config.MagnetOffset = angleOffset;
        encoder.getConfigurator().apply(config);

        // Config angle encoders
        // absoluteEncoder = angleMotor.getAbsoluteEncoder(Type.kDutyCycle);
        // absoluteEncoder.setPositionConversionFactor(Drivebase.DEGREES_PER_STEERING_ROTATION);
        // absoluteEncoder.setVelocityConversionFactor(Drivebase.DEGREES_PER_STEERING_ROTATION / 60);
        // absoluteEncoder.setZeroOffset(angleOffset);
        // absoluteEncoder.setInverted(Drivebase.ABSOLUTE_ENCODER_INVERT);
        // absoluteEncoder.setAverageDepth(1);

        // Config angle motor/controller - simon

        // Config angle motor/controller
        angleController = angleMotor.getPIDController();
        angleController.setP(Drivebase.MODULE_KP);
        angleController.setI(Drivebase.MODULE_KI);
        angleController.setD(Drivebase.MODULE_KD);
        angleController.setFF(Drivebase.MODULE_KF);
        angleController.setIZone(Drivebase.MODULE_IZ);
        angleController.setPositionPIDWrappingEnabled(true);
        angleController.setPositionPIDWrappingMaxInput(180);
        angleController.setPositionPIDWrappingMinInput(-180);
        // angleController.setFeedbackDevice(encoder);
        angleMotor.setInverted(Drivebase.ANGLE_MOTOR_INVERT);
        angleMotor.setIdleMode(CANSparkMax.IdleMode.kCoast);
        angleMotor.setSmartCurrentLimit(Drivebase.SWERVE_MODULE_CURRENT_LIMIT);

        // Config drive motor/controller
        driveController = driveMotor.getPIDController();
        driveEncoder = driveMotor.getEncoder();
        driveEncoder.setPositionConversionFactor(Drivebase.METERS_PER_MOTOR_ROTATION);
        driveEncoder.setVelocityConversionFactor(Drivebase.METERS_PER_MOTOR_ROTATION / 60);
        driveEncoder.setAverageDepth(1);
        driveController.setP(Drivebase.VELOCITY_KP);
        driveController.setI(Drivebase.VELOCITY_KI);
        driveController.setD(Drivebase.VELOCITY_KD);
        driveController.setFF(Drivebase.VELOCITY_KF);
        driveController.setIZone(Drivebase.VELOCITY_IZ);
        driveMotor.setInverted(Drivebase.DRIVE_MOTOR_INVERT);
        driveMotor.setIdleMode(CANSparkMax.IdleMode.kBrake);
        driveMotor.setSmartCurrentLimit(Drivebase.SWERVE_MODULE_CURRENT_LIMIT);

        driveMotor.burnFlash();
        angleMotor.burnFlash();

        feedforward = new SimpleMotorFeedforward(Drivebase.KS, Drivebase.KV, Drivebase.KA);

        time = new Timer();
        time.start();
        lastTime = time.get();
        
        lastAngle = getState().angle.getDegrees();
    }

    public void setDesiredState(SwerveModuleState desiredState, boolean isOpenLoop) {
        setDesiredState(desiredState, isOpenLoop, true);
    }

    public void setGains(double kp, double ki, double kd, double ks, double kv, double ka) {
        //feedforward = new SimpleMotorFeedforward(ks, kv, ka);
        //driveController.setP(kp);
        //driveController.setI(ki);
        //driveController.setD(kd);
    }
    
    public void setDesiredState(SwerveModuleState desiredState, boolean isOpenLoop, boolean antijitter) {
        desiredState = SwerveModuleState.optimize(desiredState, getState().angle);
        this.desiredState = desiredState;
        SmartDashboard.putNumber("Optimized " + moduleNumber + " Speed Setpoint: ", desiredState.speedMetersPerSecond);
        SmartDashboard.putNumber("Optimized " + moduleNumber + " Angle Setpoint: ", desiredState.angle.getDegrees());
        SmartDashboard.putNumber("Module " + moduleNumber + "CurrentDraw", driveMotor.getOutputCurrent());
        SmartDashboard.putNumber("Module " + moduleNumber + " Volts", driveMotor.getAppliedOutput() * driveMotor.getBusVoltage());
        SmartDashboard.putNumber("Module " + moduleNumber + "Speed", driveEncoder.getVelocity());

        if (isOpenLoop) {
            double percentOutput = desiredState.speedMetersPerSecond / Drivebase.MAX_SPEED;
            driveMotor.set(percentOutput);
        } else {
            double velocity = desiredState.speedMetersPerSecond;
            driveController.setReference(velocity, ControlType.kVelocity, 0, feedforward.calculate(velocity));
        }

        double angle = ((Math.abs(desiredState.speedMetersPerSecond) <= (Drivebase.MAX_SPEED * 0.01)) && antijitter ? 
            lastAngle :
            desiredState.angle.getDegrees()); // Prevents module rotation if speed is less than 1%
        angleController.setReference(angle, ControlType.kPosition);
        lastAngle = angle;

        this.angle = desiredState.angle.getDegrees();
        speed = desiredState.speedMetersPerSecond;

        if (!Robot.isReal()) {
            dt = time.get() - lastTime;
            fakePos += (speed * dt);
            lastTime = time.get();
        }
    }

    public void setAzimuth(Rotation2d azimuth) {
        angleController.setReference(azimuth.getDegrees(), ControlType.kPosition);
    }

    public void setDriveVolts(double volts) {
        driveMotor.setVoltage(volts);
    }

    public double getDriveVolts() {
        return driveMotor.getAppliedOutput() * driveMotor.getBusVoltage();
    }

    public Translation2d getModuleLocation() {
        return moduleLocation;
    }

    public SwerveModuleState getState() {
        double velocity;
        Rotation2d azimuth;
        if (Robot.isReal()) {
            velocity = driveEncoder.getVelocity();
            azimuth = Rotation2d.fromDegrees(encoder.getAbsolutePosition().getValueAsDouble() * 360); //TODO: does getAbsolutePosition return in degrees or does it need to be changed
        } else {
            velocity = speed;
            azimuth = Rotation2d.fromDegrees(this.angle);
        }
        return new SwerveModuleState(velocity, azimuth);
    }

    public SwerveModuleState getDesiredState() {
        return desiredState;
    }

    public SwerveModulePosition getPosition() {
        double position;
        Rotation2d azimuth;
        if (Robot.isReal()) {
            position = driveEncoder.getPosition();
            azimuth = Rotation2d.fromDegrees(encoder.getAbsolutePosition().getValueAsDouble() * 360); //TODO: does getAbsolutePosition return in degrees or does it need to be changed
        } else {
            position = fakePos;
            azimuth = Rotation2d.fromDegrees(angle);
        }
        SmartDashboard.putNumber("Module " + moduleNumber + "Angle", azimuth.getDegrees());
        SmartDashboard.putNumber("Module " + moduleNumber + " Wheel Pos", position);
        return new SwerveModulePosition(position, azimuth);
    }

    public double getAbsoluteEncoder() {
        return (encoder.getPosition().getValueAsDouble() * 360);
    }

    public void setMotorBrake(boolean brake) {
        driveMotor.setIdleMode(brake ? CANSparkMax.IdleMode.kBrake : CANSparkMax.IdleMode.kCoast);
    }
    
    public void turnModule(double speed) {
        angleMotor.set(speed);
        SmartDashboard.putNumber("AbsoluteEncoder" + moduleNumber, encoder.getVelocity().getValueAsDouble()); //TODO: Does this value return the correct unit
        SmartDashboard.putNumber("ControlEffort" + moduleNumber, angleMotor.getAppliedOutput());
    }
}