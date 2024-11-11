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

// Define valid ENUM values for the role
$valid_roles = ['admin', 'penulis', 'pembaca', 'peninjau'];

// Process the request based on HTTP method
switch($method) {
    case 'POST':
        // Create a new user
        $data = json_decode(file_get_contents("php://input"));
        if (!empty($data->uid) && !empty($data->nama_pengguna) && !empty($data->password) && !empty($data->email) && !empty($data->role) && !empty($data->nama_lengkap) && !empty($data->otp) && !empty($data->otp_expiry)) {
            if (!in_array($data->role, $valid_roles)) {
                send_response(400, ["message" => "Invalid role. Must be one of: 'admin', 'penulis', 'pembaca', 'peninjau'."]);
                break;
            }
    
            // Check for existing UID
            $checkQuery = "SELECT COUNT(*) as count FROM user WHERE uid = ?";
            $checkStmt = mysqli_prepare($conn, $checkQuery);
            mysqli_stmt_bind_param($checkStmt, "s", $data->uid);
            mysqli_stmt_execute($checkStmt);
            $checkResult = mysqli_stmt_get_result($checkStmt);
            $row = mysqli_fetch_assoc($checkResult);
            if ($row['count'] > 0) {
                send_response(409, ["message" => "User with this UID already exists."]);
                break;
            }
    
            // Insert new user, including otp and otp_expiry
            $query = "INSERT INTO user (uid, nama_pengguna, password, email, profile_pic, role, kredensial, nama_lengkap, otp, otp_expiry) 
                      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            $stmt = mysqli_prepare($conn, $query);
            $password = password_hash($data->password, PASSWORD_DEFAULT); // Hash password
    
            // Bind parameters
            mysqli_stmt_bind_param($stmt, "ssssssssss", $data->uid, $data->nama_pengguna, $password, $data->email, $data->profile_pic, $data->role, $data->kredensial, $data->nama_lengkap, $data->otp, $data->otp_expiry);
    
            if (mysqli_stmt_execute($stmt)) {
                send_response(201, ["message" => "User created successfully."]);
            } else {
                send_response(500, ["message" => "Failed to create user."]);
            }
        } else {
            send_response(400, ["message" => "Incomplete data. Ensure 'otp' and 'otp_expiry' are provided."]);
        }
        break;  

    case 'GET':
        // Retrieve users or a single user by UID
        if (isset($_GET['uid'])) {
            $query = "SELECT * FROM user WHERE uid = ?";
            $stmt = mysqli_prepare($conn, $query);
            mysqli_stmt_bind_param($stmt, "s", $_GET['uid']);
            mysqli_stmt_execute($stmt);
            $result = mysqli_stmt_get_result($stmt);
            $user = mysqli_fetch_assoc($result);
            send_response(200, $user ? $user : ["message" => "User not found."]);
        } else {
            $query = "SELECT * FROM user";
            $stmt = mysqli_prepare($conn, $query);
            mysqli_stmt_execute($stmt);
            $result = mysqli_stmt_get_result($stmt);
            $users = mysqli_fetch_all($result, MYSQLI_ASSOC);
            send_response(200, $users);
        }
        break;

    case 'PUT':
        // Update user details by UID or just update password
        $data = json_decode(file_get_contents("php://input"));
        
        // Mengecek apakah hanya password yang akan diupdate
        if (isset($data->password) && !empty($data->password) && isset($data->uid)) {
            // Hash password baru
            $password = password_hash($data->password, PASSWORD_DEFAULT);

            // Query untuk update password
            $query = "UPDATE user SET password = ? WHERE uid = ?";
            $stmt = mysqli_prepare($conn, $query);
            mysqli_stmt_bind_param($stmt, "ss", $password, $data->uid);

            if (mysqli_stmt_execute($stmt)) {
                send_response(200, ["message" => "Password updated successfully."]);
            } else {
                send_response(500, ["message" => "Failed to update password."]);
            }
        } else {
            // Jika tidak ada password yang diberikan
            send_response(400, ["message" => "Password and UID are required to update the password."]);
        }
        break;

    case 'DELETE':
        // Delete user by UID
        $data = json_decode(file_get_contents("php://input"));
        if (!empty($data->uid)) {
            $query = "DELETE FROM user WHERE uid = ?";
            $stmt = mysqli_prepare($conn, $query);
            mysqli_stmt_bind_param($stmt, "s", $data->uid);
            if (mysqli_stmt_execute($stmt)) {
                send_response(200, ["message" => "User deleted successfully."]);
            } else {
                send_response(500, ["message" => "Failed to delete user."]);
            }
        } else {
            send_response(400, ["message" => "UID is required."]);
        }
        break;

    default:
        // Method not allowed
        send_response(405, ["message" => "Method not allowed."]);
        break;
}
?>
