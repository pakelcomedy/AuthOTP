<?php
function getKoneksi() {
    $host = 'localhost:3307'; // or your database host
    $user = 'root'; // your database username
    $pass = ''; // your database password
    $dbname = 'signup'; // your database name

    $conn = mysqli_connect($host, $user, $pass, $dbname);
    if (!$conn) {
        return false; // Connection failed
    }
    return $conn; // Return the connection
}
?>
