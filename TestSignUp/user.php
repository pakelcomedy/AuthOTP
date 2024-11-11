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
header("Access-Control-Allow-Methods: GET");
header("Access-Control-Allow-Headers: Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");

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

// Check if UID parameter is provided in the URL
if (isset($_GET['uid'])) {
    // Retrieve a single user by UID
    $uid = $_GET['uid'];
    $query = "SELECT * FROM user WHERE uid = ?";
    $stmt = mysqli_prepare($conn, $query);
    mysqli_stmt_bind_param($stmt, "s", $uid);
    mysqli_stmt_execute($stmt);
    $result = mysqli_stmt_get_result($stmt);
    $user = mysqli_fetch_assoc($result);

    if ($user) {
        send_response(200, $user);
    } else {
        send_response(404, ["message" => "User not found."]);
    }
} else {
    // Retrieve all users if no UID is provided
    $query = "SELECT * FROM user";
    $stmt = mysqli_prepare($conn, $query);
    mysqli_stmt_execute($stmt);
    $result = mysqli_stmt_get_result($stmt);
    $users = mysqli_fetch_all($result, MYSQLI_ASSOC);

    send_response(200, $users);
}

// Close the connection
mysqli_close($conn);
?>
