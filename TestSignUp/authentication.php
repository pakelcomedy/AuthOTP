<?php
// Enable error reporting for debugging
ini_set('display_errors', 1);
ini_set('display_startup_errors', 1);
error_reporting(E_ALL);

// Include the database connection
include 'koneksi.php'; 

// Headers for allowing CORS and JSON response format
header("Access-Control-Allow-Origin: *");
header("Content-Type: application/json; charset=UTF-8");
header("Access-Control-Allow-Methods: POST, GET, PUT, DELETE");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

// Read the HTTP method
$method = $_SERVER['REQUEST_METHOD'];

// Helper function to send JSON response
function send_response($status_code, $data) {
    http_response_code($status_code);
    echo json_encode($data);
}

// Get the database connection
$conn = getKoneksi();

// Check if the connection was successful
if (!$conn) {
    send_response(500, ["message" => "Database connection failed."]);
    exit;
}

// Process the request based on HTTP method
switch($method) {
    case 'GET':
        // Retrieve user by email
        if (isset($_GET['email'])) {
            $email = $_GET['email'];

            // Sanitize email input to prevent SQL injection
            $email = mysqli_real_escape_string($conn, $email);

            // Prepare the SQL query
            $query = "SELECT * FROM user WHERE email = ?";
            $stmt = mysqli_prepare($conn, $query);
            mysqli_stmt_bind_param($stmt, "s", $email);
            mysqli_stmt_execute($stmt);

            // Get the result
            $result = mysqli_stmt_get_result($stmt);
            $user = mysqli_fetch_assoc($result);

            if ($user) {
                // Return user data if found
                send_response(200, $user);
            } else {
                // Return message if user not found
                send_response(404, ["message" => "User not found."]);
            }
        } else {
            // Missing email parameter
            send_response(400, ["message" => "Email parameter is required."]);
        }
        break;

    case 'POST':
        // Handle OTP verification and resending OTP
        $data = json_decode(file_get_contents("php://input"), true);

        if (isset($data['otp']) && isset($data['email'])) {
            // OTP verification logic
            $otp = $data['otp'];
            $email = $data['email'];

            $query = "SELECT otp FROM user WHERE email = ? AND otp = ?";
            $stmt = mysqli_prepare($conn, $query);
            mysqli_stmt_bind_param($stmt, "ss", $email, $otp);
            mysqli_stmt_execute($stmt);
            $result = mysqli_stmt_get_result($stmt);

            if (mysqli_num_rows($result) > 0) {
                send_response(200, ["message" => "OTP verified successfully."]);
            } else {
                send_response(400, ["message" => "Invalid OTP."]);
            }
        } elseif (isset($data['email'])) {
            // Resend OTP logic
            $email = $data['email'];

            // Generate a new OTP and send it via email (simulated)
            $newOtp = rand(100000, 999999);  // Simulate new OTP generation

            // Update the OTP in the database
            $query = "UPDATE user SET otp = ? WHERE email = ?";
            $stmt = mysqli_prepare($conn, $query);
            mysqli_stmt_bind_param($stmt, "ss", $newOtp, $email);
            $success = mysqli_stmt_execute($stmt);

            if ($success) {
                send_response(200, ["message" => "OTP resent successfully."]);
            } else {
                send_response(400, ["message" => "Failed to resend OTP."]);
            }
        } else {
            send_response(400, ["message" => "OTP or email is required."]);
        }
        break;

    case 'PUT':
        // Handle password update
        $data = json_decode(file_get_contents("php://input"), true);

        if (isset($data['email']) && isset($data['password'])) {
            $email = $data['email'];
            $password = $data['password'];

            // Hash the password (for security)
            $hashedPassword = password_hash($password, PASSWORD_DEFAULT);

            // Update the password in the database
            $query = "UPDATE user SET password = ? WHERE email = ?";
            $stmt = mysqli_prepare($conn, $query);
            mysqli_stmt_bind_param($stmt, "ss", $hashedPassword, $email);
            $success = mysqli_stmt_execute($stmt);

            if ($success) {
                send_response(200, ["message" => "Password updated successfully."]);
            } else {
                send_response(400, ["message" => "Failed to update password."]);
            }
        } else {
            send_response(400, ["message" => "Email and password are required."]);
        }
        break;

    default:
        // Method not allowed
        send_response(405, ["message" => "Method not allowed."]);
        break;
}
?>