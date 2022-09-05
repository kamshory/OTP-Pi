<?php

function generateKey($password, $salt)
{
    if(strlen($password) > 8)
    {
        $password = substr($password, strlen($password) - 8);
    }
    else
    {
        $password = sprintf("%8s", $password);
    }
    if(strlen($salt) > 8)
    {
        $salt = substr($salt, strlen($salt) - 8);
    }
    else
    {
        $salt = sprintf("%8s", $salt);
    }
	
    return openssl_pbkdf2($password, $salt, 32, 65536, 'sha256');
}
function generateRandomInitvector()
{
    return openssl_random_pseudo_bytes(16, $crypto_strong);
}

function base64Encoding($input)
{
    return base64_encode($input);
}

function base64Decoding($input)
{
    return base64_decode($input);
}

function aesEncrypt($key, $data)
{
    $iv = generateRandomInitvector();
    $ciphertext = openssl_encrypt($data, 'aes-256-cbc', $key, OPENSSL_RAW_DATA, $iv);
    return base64_encode($iv . $ciphertext);
}

function aesDecrypt($key, $data)
{
    $iv = substr($data, 0, 16);
    $encryptedData = substr($data, 16);
    return openssl_decrypt($encryptedData, 'aes-256-cbc', $key, OPENSSL_RAW_DATA, $iv);
}

function uuid() {
    return sprintf( '%04x%04x-%04x-%04x-%04x-%04x%04x%04x',
        // 32 bits for "time_low"
        mt_rand( 0, 0xffff ), mt_rand( 0, 0xffff ),

        // 16 bits for "time_mid"
        mt_rand( 0, 0xffff ),

        // 16 bits for "time_hi_and_version",
        // four most significant bits holds version number 4
        mt_rand( 0, 0x0fff ) | 0x4000,

        // 16 bits, 8 bits for "clk_seq_hi_res",
        // 8 bits for "clk_seq_low",
        // two most significant bits holds zero and one for variant DCE1.1
        mt_rand( 0, 0x3fff ) | 0x8000,

        // 48 bits for "node"
        mt_rand( 0, 0xffff ), mt_rand( 0, 0xffff ), mt_rand( 0, 0xffff )
    );
}

function tlvBuild($data)
{
	$result = "";
	foreach($data as $key=>$value)
	{
		$tag = substr(sprintf("%2s", $key), 0, 2);
		$length = strlen($value);
		$result .= sprintf("%2s%02x%s", $tag, $length, $value);
	}
	return $result;
}

$password = "kiIJujED";
$salt = "IjYTWsrJ";

$requestBody = file_get_contents("php://input");

$requestJSON = json_decode($requestBody, true);

$salt = !empty($requestJSON['cpusn'])?$requestJSON['cpusn']:$salt;


$activationCode = tlvBuild(array(
	"NM"=>@$requestJSON['name'],
	"EM"=>@$requestJSON['email'],
	"PH"=>@$requestJSON['phone'],
	"VR"=>"1",
	"AC"=>uuid(),
	"TS"=>time()
	)
);

$key = generateKey($password, $salt);

$activationCodeEncrypted = aesEncrypt($key, $activationCode);

$resposeJSON = json_encode(array(
	"command"=>"activation",
	"data"=>array(
		"activation_code"=>$activationCodeEncrypted
	)
	)
);
header("Content-type: application/json");
header("Content-length: ".strlen($resposeJSON));

echo $resposeJSON;
