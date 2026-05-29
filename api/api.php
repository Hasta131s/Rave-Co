<?php
/**
 * RAVE CO BACKEND API - BOSFORLAB.ONLINE
 * Place this single PHP file on your hosting space (e.g., bosforlab.online/api.php)
 * This script will automatically create an SQLite database named "rave_co.db" inside its directory.
 * If you prefer MySQL, configure the credentials below.
 */

header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Headers: Content-Type, Authorization, X-Requested-With');
header('Access-Control-Allow-Methods: GET, POST, OPTIONS');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    exit(0);
}

// DATABASE CONFIGURATION
define('USE_SQLITE', true); // Set to false to use MySQL

define('MYSQL_HOST', 'localhost');
define('MYSQL_DB', 'bosforla_raveco');
define('MYSQL_USER', 'bosforla_user');
define('MYSQL_PASS', 'mysql_password');

// DATABASE CONNECTION
try {
    if (USE_SQLITE) {
        $db_file = __DIR__ . '/rave_co.db';
        $db = new PDO("sqlite:" . $db_file);
        $db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
        // Enable Foreign Keys in SQLite
        $db->exec("PRAGMA foreign_keys = ON;");
    } else {
        $db = new PDO("mysql:host=" . MYSQL_HOST . ";dbname=" . MYSQL_DB . ";charset=utf8", MYSQL_USER, MYSQL_PASS);
        $db->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    }
} catch (PDOException $e) {
    echo json_encode(['success' => false, 'error' => 'Database connection failed: ' . $e->getMessage()]);
    exit();
}

// CREATE TABLES IF THEY DO NOT EXIST (AUTO-INITIALIZATION)
try {
    // 1. Users
    if (USE_SQLITE) {
        $db->exec("CREATE TABLE IF NOT EXISTS users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE NOT NULL,
            password TEXT NOT NULL,
            avatar TEXT DEFAULT 'avatar1',
            status_text TEXT DEFAULT 'Raving!',
            last_active INTEGER DEFAULT 0
        );");

        // 2. Friendships
        $db->exec("CREATE TABLE IF NOT EXISTS friends (
            user_id INTEGER,
            friend_id INTEGER,
            status TEXT DEFAULT 'pending', -- pending, accepted
            PRIMARY KEY (user_id, friend_id)
        );");

        // 3. Rooms
        $db->exec("CREATE TABLE IF NOT EXISTS rooms (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            name TEXT NOT NULL,
            owner_id INTEGER NOT NULL,
            video_url TEXT DEFAULT '',
            video_title TEXT DEFAULT '',
            is_playing INTEGER DEFAULT 0, -- 0 or 1
            playback_time REAL DEFAULT 0.0,
            last_sync_time INTEGER DEFAULT 0,
            FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE CASCADE
        );");

        // 4. Room Participants / Moderation States
        $db->exec("CREATE TABLE IF NOT EXISTS room_participants (
            room_id INTEGER,
            user_id INTEGER,
            role TEXT DEFAULT 'member', -- owner, moderator, member
            is_muted INTEGER DEFAULT 0, -- 0 or 1
            last_seen INTEGER DEFAULT 0,
            PRIMARY KEY (room_id, user_id),
            FOREIGN KEY(room_id) REFERENCES rooms(id) ON DELETE CASCADE,
            FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
        );");

        // 5. Room Chat Messages
        $db->exec("CREATE TABLE IF NOT EXISTS room_messages (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            room_id INTEGER,
            user_id INTEGER,
            message TEXT NOT NULL,
            is_system INTEGER DEFAULT 0, -- 0=user, 1=system joins/leaves/moderation
            timestamp INTEGER DEFAULT 0,
            FOREIGN KEY(room_id) REFERENCES rooms(id) ON DELETE CASCADE,
            FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
        );");

        // 6. Direct Messages (DM)
        $db->exec("CREATE TABLE IF NOT EXISTS direct_messages (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            sender_id INTEGER,
            receiver_id INTEGER,
            message TEXT NOT NULL,
            timestamp INTEGER DEFAULT 0,
            is_read INTEGER DEFAULT 0,
            FOREIGN KEY(sender_id) REFERENCES users(id) ON DELETE CASCADE,
            FOREIGN KEY(receiver_id) REFERENCES users(id) ON DELETE CASCADE
        );");

        // 7. Kicked user tracking (To avoid immediate rejoining block)
        $db->exec("CREATE TABLE IF NOT EXISTS kicked_users (
            room_id INTEGER,
            user_id INTEGER,
            kicked_at INTEGER DEFAULT 0,
            PRIMARY KEY (room_id, user_id)
        );");
    } else {
        // MySQL equivalents
        $db->exec("CREATE TABLE IF NOT EXISTS users (
            id INT AUTO_INCREMENT PRIMARY KEY,
            username VARCHAR(50) UNIQUE NOT NULL,
            password VARCHAR(255) NOT NULL,
            avatar VARCHAR(50) DEFAULT 'avatar1',
            status_text VARCHAR(100) DEFAULT 'Raving!',
            last_active BIGINT DEFAULT 0
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        $db->exec("CREATE TABLE IF NOT EXISTS friends (
            user_id INT,
            friend_id INT,
            status VARCHAR(20) DEFAULT 'pending',
            PRIMARY KEY (user_id, friend_id)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        $db->exec("CREATE TABLE IF NOT EXISTS rooms (
            id INT AUTO_INCREMENT PRIMARY KEY,
            name VARCHAR(100) NOT NULL,
            owner_id INT NOT NULL,
            video_url TEXT,
            video_title VARCHAR(255) DEFAULT '',
            is_playing INT DEFAULT 0,
            playback_time DOUBLE DEFAULT 0.0,
            last_sync_time BIGINT DEFAULT 0,
            FOREIGN KEY(owner_id) REFERENCES users(id) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        $db->exec("CREATE TABLE IF NOT EXISTS room_participants (
            room_id INT,
            user_id INT,
            role VARCHAR(20) DEFAULT 'member',
            is_muted INT DEFAULT 0,
            last_seen BIGINT DEFAULT 0,
            PRIMARY KEY (room_id, user_id),
            FOREIGN KEY(room_id) REFERENCES rooms(id) ON DELETE CASCADE,
            FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        $db->exec("CREATE TABLE IF NOT EXISTS room_messages (
            id INT AUTO_INCREMENT PRIMARY KEY,
            room_id INT,
            user_id INT,
            message TEXT NOT NULL,
            is_system INT DEFAULT 0,
            timestamp BIGINT DEFAULT 0,
            FOREIGN KEY(room_id) REFERENCES rooms(id) ON DELETE CASCADE,
            FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        $db->exec("CREATE TABLE IF NOT EXISTS direct_messages (
            id INT AUTO_INCREMENT PRIMARY KEY,
            sender_id INT,
            receiver_id INT,
            message TEXT NOT NULL,
            timestamp BIGINT DEFAULT 0,
            is_read INT DEFAULT 0,
            FOREIGN KEY(sender_id) REFERENCES users(id) ON DELETE CASCADE,
            FOREIGN KEY(receiver_id) REFERENCES users(id) ON DELETE CASCADE
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");

        $db->exec("CREATE TABLE IF NOT EXISTS kicked_users (
            room_id INT,
            user_id INT,
            kicked_at BIGINT DEFAULT 0,
            PRIMARY KEY (room_id, user_id)
        ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
    }

    // ALTER room_messages to support replies and deletions on both SQLite and MySQL
    try {
        $db->exec("ALTER TABLE room_messages ADD COLUMN reply_to_id INT DEFAULT NULL;");
    } catch (Exception $e) {}
    try {
        $db->exec("ALTER TABLE room_messages ADD COLUMN reply_to_name VARCHAR(255) DEFAULT NULL;");
    } catch (Exception $e) {}
    try {
        $db->exec("ALTER TABLE room_messages ADD COLUMN reply_to_msg TEXT DEFAULT NULL;");
    } catch (Exception $e) {}
    try {
        $db->exec("ALTER TABLE room_messages ADD COLUMN is_deleted INT DEFAULT 0;");
    } catch (Exception $e) {}

} catch (PDOException $e) {
    echo json_encode(['success' => false, 'error' => 'Database initialization error: ' . $e->getMessage()]);
    exit();
}

// UTILITY FUNCTION TO CLEAN STALE USERS/ROOMS
$currentTime = time();
// Update users last_active as part of API calls or tick
// Keep track of active users inside participants (cleanup if not seen in 15 seconds)
try {
    $stale_timeout = $currentTime - 15;
    
    // Find rooms that will have system messages for disconnects before deleting
    $inactive_stmt = $db->prepare("SELECT room_id, user_id, u.username FROM room_participants rp JOIN users u ON rp.user_id = u.id WHERE last_seen < :stale_timeout");
    $inactive_stmt->execute([':stale_timeout' => $stale_timeout]);
    $inactive_users = $inactive_stmt->fetchAll(PDO::FETCH_ASSOC);
    
    foreach ($inactive_users as $iu) {
        // Add system message: username left
        $sys_stmt = $db->prepare("INSERT INTO room_messages (room_id, user_id, message, is_system, timestamp) VALUES (:room_id, :user_id, :msg, 1, :ts)");
        $sys_stmt->execute([
            ':room_id' => $iu['room_id'],
            ':user_id' => $iu['user_id'],
            ':msg' => "system_left:" . $iu['username'],
            ':ts' => $currentTime
        ]);
    }
    
    // Delete stale participant records
    $del_part = $db->prepare("DELETE FROM room_participants WHERE last_seen < :stale_timeout");
    $del_part->execute([':stale_timeout' => $stale_timeout]);
    
    // Delete empty rooms (owner not active or 0 participants for 3 minutes)
    // To keep simple, we'll delete rooms with 0 active participants
    $empty_rooms_stmt = $db->prepare("SELECT id FROM rooms WHERE id NOT IN (SELECT DISTINCT room_id FROM room_participants)");
    $empty_rooms_stmt->execute();
    $empty_rooms = $empty_rooms_stmt->fetchAll(PDO::FETCH_COLUMN);
    
    if (!empty($empty_rooms)) {
        $in_clause = implode(',', array_fill(0, count($empty_rooms), '?'));
        $del_rooms_stmt = $db->prepare("DELETE FROM rooms WHERE id IN ($in_clause)");
        $del_rooms_stmt->execute($empty_rooms);
    }
} catch (Exception $ex) {
    // Suppress clean issues
}

// REQUEST PARSING
$action = isset($_GET['action']) ? $_GET['action'] : '';
$input = json_decode(file_get_contents('php://input'), true);

if (!$input) {
    $input = $_POST;
}

// HELPER FOR JSON RESPONSES
function respond($success, $data = [], $error = null) {
    echo json_encode(array_merge(['success' => (bool)$success], $data, $error ? ['error' => $error] : []));
    exit();
}

switch ($action) {
    case 'register':
        $username = trim($input['username'] ?? '');
        $password = $input['password'] ?? '';
        $avatar = $input['avatar'] ?? 'avatar1';
        
        if (strlen($username) < 3 || strlen($password) < 4) {
            respond(false, [], "Kullanıcı adı en az 3, şifre en az 4 karakter olmalıdır.");
        }
        
        // Check if username exists
        $chk = $db->prepare("SELECT id FROM users WHERE username = :u");
        $chk->execute([':u' => $username]);
        if ($chk->fetch()) {
            respond(false, [], "Bu kullanıcı adı zaten alınmış.");
        }
        
        $hashed_password = password_hash($password, PASSWORD_DEFAULT);
        $ins = $db->prepare("INSERT INTO users (username, password, avatar, last_active) VALUES (:u, :p, :a, :la)");
        $ins->execute([
            ':u' => $username,
            ':p' => $hashed_password,
            ':a' => $avatar,
            ':la' => time()
        ]);
        
        $userId = $db->lastInsertId();
        respond(true, ['userId' => (int)$userId, 'username' => $username, 'avatar' => $avatar]);
        break;

    case 'login':
        $username = trim($input['username'] ?? '');
        $password = $input['password'] ?? '';
        
        if (empty($username) || empty($password)) {
            respond(false, [], "Lütfen kullanıcı adı ve şifre girin.");
        }
        
        $stmt = $db->prepare("SELECT * FROM users WHERE username = :u");
        $stmt->execute([':u' => $username]);
        $user = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if (!$user || !password_verify($password, $user['password'])) {
            respond(false, [], "Kullanıcı adı veya şifre hatalı.");
        }
        
        // Update last active
        $upd = $db->prepare("UPDATE users SET last_active = :t WHERE id = :id");
        $upd->execute([':t' => time(), ':id' => $user['id']]);
        
        respond(true, [
            'userId' => (int)$user['id'],
            'username' => $user['username'],
            'avatar' => $user['avatar'],
            'statusText' => $user['status_text']
        ]);
        break;

    case 'get_profile':
        $userId = (int)($input['userId'] ?? 0);
        $targetId = (int)($input['targetId'] ?? 0);
        
        if ($targetId <= 0) $targetId = $userId;
        
        $stmt = $db->prepare("SELECT id, username, avatar, status_text FROM users WHERE id = :id");
        $stmt->execute([':id' => $targetId]);
        $profile = $stmt->fetch(PDO::FETCH_ASSOC);
        
        if (!$profile) {
            respond(false, [], "Profil bulunamadı.");
        }
        
        // Friend status
        $friend_status = "none";
        if ($userId != $targetId) {
            // Check both directions
            $stmt_f = $db->prepare("SELECT status, user_id FROM friends WHERE (user_id = :u AND friend_id = :t) OR (user_id = :t AND friend_id = :u)");
            $stmt_f->execute([':u' => $userId, ':t' => $targetId]);
            $f = $stmt_f->fetch(PDO::FETCH_ASSOC);
            if ($f) {
                if ($f['status'] === 'accepted') {
                    $friend_status = "friend";
                } else {
                    $friend_status = ($f['user_id'] == $userId) ? "sent_pending" : "received_pending";
                }
            }
        }
        
        respond(true, [
            'profile' => [
                'userId' => (int)$profile['id'],
                'username' => $profile['username'],
                'avatar' => $profile['avatar'],
                'statusText' => $profile['status_text'],
                'friendStatus' => $friend_status
            ]
        ]);
        break;

    case 'update_profile':
        $userId = (int)($input['userId'] ?? 0);
        $avatar = $input['avatar'] ?? null;
        $status_text = $input['statusText'] ?? null;
        $password = $input['password'] ?? null;
        
        if ($userId <= 0) respond(false, [], "Geçersiz yetki.");
        
        $params = [];
        $query_parts = [];
        
        if ($avatar !== null) {
            $query_parts[] = "avatar = :avatar";
            $params[':avatar'] = $avatar;
        }
        if ($status_text !== null) {
            $query_parts[] = "status_text = :status_text";
            $params[':status_text'] = $status_text;
        }
        if (!empty($password)) {
            $query_parts[] = "password = :password";
            $params[':password'] = password_hash($password, PASSWORD_DEFAULT);
        }
        
        if (empty($query_parts)) {
            respond(false, [], "Güncellenecek veri sağlanmadı.");
        }
        
        $params[':id'] = $userId;
        $query = "UPDATE users SET " . implode(", ", $query_parts) . " WHERE id = :id";
        
        $upd = $db->prepare($query);
        $upd->execute($params);
        
        respond(true, ['message' => 'Profil güncellendi']);
        break;

    case 'rooms':
        // List active rooms
        $userId = (int)($input['userId'] ?? 0);
        
        $stmt = $db->prepare("
            SELECT r.*, u.username as owner_name, u.avatar as owner_avatar,
            (SELECT COUNT(*) FROM room_participants WHERE room_id = r.id) as participant_count
            FROM rooms r
            JOIN users u ON r.owner_id = u.id
            ORDER BY r.id DESC
        ");
        $stmt->execute();
        $rooms = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        // Map types correctly
        $out_rooms = [];
        foreach ($rooms as $r) {
            $out_rooms[] = [
                'id' => (int)$r['id'],
                'name' => $r['name'],
                'ownerId' => (int)$r['owner_id'],
                'ownerName' => $r['owner_name'],
                'ownerAvatar' => $r['owner_avatar'],
                'videoUrl' => $r['video_url'] ?: '',
                'videoTitle' => $r['video_title'] ?: '',
                'isPlaying' => (int)$r['is_playing'] === 1,
                'playbackTime' => (float)$r['playback_time'],
                'participantCount' => (int)$r['participant_count']
            ];
        }
        
        respond(true, ['rooms' => $out_rooms]);
        break;

    case 'create_room':
        $userId = (int)($input['userId'] ?? 0);
        $name = trim($input['name'] ?? '');
        $videoUrl = trim($input['videoUrl'] ?? '');
        $videoTitle = trim($input['videoTitle'] ?? '');
        
        if ($userId <= 0) respond(false, [], "Geçersiz yetki.");
        if (empty($name)) respond(false, [], "Oda adı boş olamaz.");
        
        // Auto handle duplicates or close previous rooms by same user to keep database clean
        $db->prepare("DELETE FROM rooms WHERE owner_id = :o")->execute([':o' => $userId]);
        
        $ins = $db->prepare("INSERT INTO rooms (name, owner_id, video_url, video_title, is_playing, playback_time, last_sync_time) VALUES (:n, :o, :vu, :vt, 0, 0.0, :la)");
        $ins->execute([
            ':n' => $name,
            ':o' => $userId,
            ':vu' => $videoUrl,
            ':vt' => $videoTitle ?: ($videoUrl ? 'Rave Live Video' : 'Oynatılacak Video Seçilmedi'),
            ':la' => time()
        ]);
        
        $roomId = $db->lastInsertId();
        
        // Add owner as active participant
        $p_stmt = $db->prepare("INSERT INTO room_participants (room_id, user_id, role, last_seen) VALUES (:rid, :uid, 'owner', :now)");
        $p_stmt->execute([
            ':rid' => $roomId,
            ':uid' => $userId,
            ':now' => time()
        ]);
        
        respond(true, ['roomId' => (int)$roomId]);
        break;

    case 'join_room':
        $userId = (int)($input['userId'] ?? 0);
        $roomId = (int)($input['roomId'] ?? 0);
        
        if ($userId <= 0 || $roomId <= 0) respond(false, [], "Geçersiz veri.");
        
        // Check if room exists
        $stmt_r = $db->prepare("SELECT name FROM rooms WHERE id = :rid");
        $stmt_r->execute([':rid' => $roomId]);
        $room = $stmt_r->fetch();
        if (!$room) respond(false, [], "Oda artık aktif değil.");
        
        // Check if kicked
        $stmt_k = $db->prepare("SELECT kicked_at FROM kicked_users WHERE room_id = :rid AND user_id = :uid");
        $stmt_k->execute([':rid' => $roomId, ':uid' => $userId]);
        if ($stmt_k->fetch()) {
            respond(false, [], "Bu odadan uzaklaştırıldınız (Atıldınız).");
        }
        
        // Find if already participant
        $stmt_p = $db->prepare("SELECT role, is_muted FROM room_participants WHERE room_id = :rid AND user_id = :uid");
        $stmt_p->execute([':rid' => $roomId, ':uid' => $userId]);
        $part = $stmt_p->fetch(PDO::FETCH_ASSOC);
        
        $username_stmt = $db->prepare("SELECT username FROM users WHERE id = :id");
        $username_stmt->execute([':id' => $userId]);
        $u = $username_stmt->fetch();
        $username = $u['username'] ?? 'User';
        
        if (!$part) {
            // Join as member
            $p_stmt = $db->prepare("INSERT OR REPLACE INTO room_participants (room_id, user_id, role, last_seen) VALUES (:rid, :uid, 'member', :now)");
            $p_stmt->execute([
                ':rid' => $roomId,
                ':uid' => $userId,
                ':now' => time()
            ]);
            
            // Add system join message
            $sys_stmt = $db->prepare("INSERT INTO room_messages (room_id, user_id, message, is_system, timestamp) VALUES (:rid, :uid, :msg, 1, :ts)");
            $sys_stmt->execute([
                ':rid' => $roomId,
                ':uid' => $userId,
                ':msg' => "system_join:" . $username,
                ':ts' => time()
            ]);
        } else {
            // Update last_seen
            $upd = $db->prepare("UPDATE room_participants SET last_seen = :ts WHERE room_id = :rid AND user_id = :uid");
            $upd->execute([':ts' => time(), ':rid' => $roomId, ':uid' => $userId]);
        }
        
        respond(true, ['roomId' => $roomId, 'joined' => true]);
        break;

    case 'leave_room':
        $userId = (int)($input['userId'] ?? 0);
        $roomId = (int)($input['roomId'] ?? 0);
        
        if ($userId <= 0 || $roomId <= 0) respond(true); // Graceful return
        
        // Remove from database
        $stmt_username = $db->prepare("SELECT username FROM users WHERE id = :id");
        $stmt_username->execute([':id' => $userId]);
        $u_name = $stmt_username->fetchColumn() ?: 'User';
        
        // Add left message
        $sys_stmt = $db->prepare("INSERT INTO room_messages (room_id, user_id, message, is_system, timestamp) VALUES (:rid, :uid, :msg, 1, :ts)");
        $sys_stmt->execute([
            ':rid' => $roomId,
            ':uid' => $userId,
            ':msg' => "system_left:" . $u_name,
            ':ts' => time()
        ]);
        
        $del = $db->prepare("DELETE FROM room_participants WHERE room_id = :rid AND user_id = :uid");
        $del->execute([':rid' => $roomId, ':uid' => $userId]);
        
        // If owner left, assign new owner or delete room
        $stmt_owner = $db->prepare("SELECT owner_id FROM rooms WHERE id = :rid");
        $stmt_owner->execute([':rid' => $roomId]);
        $owner_id = (int)$stmt_owner->fetchColumn();
        
        if ($owner_id === $userId) {
            // Assign next active moderator or member, or delete room
            $stmt_next = $db->prepare("SELECT user_id, role FROM room_participants WHERE room_id = :rid ORDER BY role = 'moderator' DESC, joined_at ASC LIMIT 1");
            // Note: SQLite doesn't have joined_at in schema, but ordering by rowid is implicit creation order or fine
            $stmt_next->execute([':rid' => $roomId]);
            $next = $stmt_next->fetch(PDO::FETCH_ASSOC);
            
            if ($next) {
                // Set as owner
                $upd_r = $db->prepare("UPDATE rooms SET owner_id = :next_uid WHERE id = :rid");
                $upd_r->execute([':next_uid' => $next['user_id'], ':rid' => $roomId]);
                
                $upd_p = $db->prepare("UPDATE room_participants SET role = 'owner' WHERE room_id = :rid AND user_id = :next_uid");
                $upd_p->execute([':rid' => $roomId, ':next_uid' => $next['user_id']]);
                
                // Add system owner transfer message
                $stmt_next_name = $db->prepare("SELECT username FROM users WHERE id = :id");
                $stmt_next_name->execute([':id' => $next['user_id']]);
                $next_name = $stmt_next_name->fetchColumn() ?: 'User';
                
                $sys_transfer = $db->prepare("INSERT INTO room_messages (room_id, user_id, message, is_system, timestamp) VALUES (:rid, NULL, :msg, 1, :ts)");
                $sys_transfer->execute([
                    ':rid' => $roomId,
                    ':msg' => "system_transfer:" . $next_name,
                    ':ts' => time()
                ]);
            } else {
                // Delete empty room
                $db->prepare("DELETE FROM rooms WHERE id = :rid")->execute([':rid' => $roomId]);
            }
        }
        
        respond(true);
        break;

    case 'room_sync':
        $userId = (int)($input['userId'] ?? 0);
        $roomId = (int)($input['roomId'] ?? 0);
        $playbackTime = (float)($input['playbackTime'] ?? 0.0);
        $isPlaying = (int)($input['isPlaying'] ?? 0); // 1 = true, 0 = false
        $lastMessageId = (int)($input['lastMessageId'] ?? 0);
        
        if ($userId <= 0 || $roomId <= 0) respond(false, [], "Geçersiz yetki veya oda.");
        
        // 1. Check if room exists
        $stmt_r = $db->prepare("SELECT * FROM rooms WHERE id = :rid");
        $stmt_r->execute([':rid' => $roomId]);
        $room = $stmt_r->fetch(PDO::FETCH_ASSOC);
        if (!$room) respond(false, ['kicked' => true], "Oda kapandı.");
        
        // 2. Refresh participation tick & mute check
        $stmt_p = $db->prepare("SELECT role, is_muted FROM room_participants WHERE room_id = :rid AND user_id = :uid");
        $stmt_p->execute([':rid' => $roomId, ':uid' => $userId]);
        $part = $stmt_p->fetch(PDO::FETCH_ASSOC);
        
        if (!$part) {
            // Check if kicked
            $stmt_k = $db->prepare("SELECT kicked_at FROM kicked_users WHERE room_id = :rid AND user_id = :uid");
            $stmt_k->execute([':rid' => $roomId, ':uid' => $userId]);
            if ($stmt_k->fetch()) {
                respond(true, ['kicked' => true], "Uzaklaştırıldınız.");
            }
            respond(false, ['kicked' => true], "Odada değilsiniz.");
        }
        
        // Update user heartbeat
        $upd_p = $db->prepare("UPDATE room_participants SET last_seen = :now WHERE room_id = :rid AND user_id = :uid");
        $upd_p->execute([':now' => time(), ':rid' => $roomId, ':uid' => $userId]);
        
        $role = $part['role'];
        $isMuted = (int)$part['is_muted'] === 1;
        
        // 3. Sync Logic
        // If owner (or co-hosts depending on design, here owner or moderator with edit rights can update room position)
        $can_control = ($role === 'owner' || $role === 'moderator');
        
        if ($can_control && isset($input['owner_sync'])) {
            // Owner is pushing authority state
            $videoUrl = $input['videoUrl'] ?? null;
            $videoTitle = $input['videoTitle'] ?? null;
            
            $fields = ["playback_time = :pt", "is_playing = :ip", "last_sync_time = :ts"];
            $params = [
                ':pt' => $playbackTime,
                ':ip' => $isPlaying,
                ':ts' => time(),
                ':rid' => $roomId
            ];
            
            if ($videoUrl !== null && $videoUrl !== "") {
                $fields[] = "video_url = :vu";
                $params[':vu'] = $videoUrl;
                
                // Add system video changed message if different from current
                if ($room['video_url'] !== $videoUrl) {
                    $v_title = $videoTitle ?: "Yeni Video";
                    $fields[] = "video_title = :vt";
                    $params[':vt'] = $v_title;
                    
                    // Add message
                    $sys_vid = $db->prepare("INSERT INTO room_messages (room_id, user_id, message, is_system, timestamp) VALUES (:rid, NULL, :msg, 1, :ts)");
                    $sys_vid->execute([
                        ':rid' => $roomId,
                        ':msg' => "system_video:" . $v_title,
                        ':ts' => time()
                    ]);
                }
            } else if ($videoTitle !== null && $videoTitle !== "") {
                $fields[] = "video_title = :vt";
                $params[':vt'] = $videoTitle;
            }
            
            $upd_r = $db->prepare("UPDATE rooms SET " . implode(", ", $fields) . " WHERE id = :rid");
            $upd_r->execute($params);
            
            // Reload updated state
            $room['playback_time'] = $playbackTime;
            $room['is_playing'] = $isPlaying;
            if ($videoUrl !== null && $videoUrl !== "") {
                $room['video_url'] = $videoUrl;
                $room['video_title'] = $videoTitle ?: "Yeni Video";
            }
        }
        
        // 4. Fetch Active Participants
        $stmt_parts = $db->prepare("
            SELECT rp.user_id, rp.role, rp.is_muted, u.username, u.avatar
            FROM room_participants rp
            JOIN users u ON rp.user_id = u.id
            WHERE rp.room_id = :rid
        ");
        $stmt_parts->execute([':rid' => $roomId]);
        $participants_raw = $stmt_parts->fetchAll(PDO::FETCH_ASSOC);
        
        $participants = [];
        foreach ($participants_raw as $p) {
            $participants[] = [
                'userId' => (int)$p['user_id'],
                'username' => $p['username'],
                'avatar' => $p['avatar'],
                'role' => $p['role'],
                'isMuted' => (int)$p['is_muted'] === 1
            ];
        }
        
        // 5. Fetch Latest 100 Messages (Chronological ascending)
        $stmt_msgs = $db->prepare("
            SELECT rm.*, u.username as sender_name, u.avatar as sender_avatar
            FROM (
                SELECT * FROM room_messages 
                WHERE room_id = :rid 
                ORDER BY id DESC LIMIT 100
            ) rm
            LEFT JOIN users u ON rm.user_id = u.id
            ORDER BY rm.id ASC
        ");
        $stmt_msgs->execute([':rid' => $roomId]);
        $new_messages_raw = $stmt_msgs->fetchAll(PDO::FETCH_ASSOC);
        
        $new_messages = [];
        foreach ($new_messages_raw as $m) {
            $new_messages[] = [
                'id' => (int)$m['id'],
                'roomId' => (int)$m['room_id'],
                'userId' => (int)$m['user_id'],
                'senderName' => $m['sender_name'] ?: ($m['is_system'] ? 'Sistem' : 'Bilinmeyen Kullanıcı'),
                'senderAvatar' => $m['sender_avatar'] ?: 'avatar1',
                'message' => $m['message'],
                'isSystem' => (int)$m['is_system'] === 1,
                'timestamp' => (int)$m['timestamp'],
                'isDeleted' => isset($m['is_deleted']) ? (int)$m['is_deleted'] === 1 : false,
                'replyToId' => isset($m['reply_to_id']) ? (int)$m['reply_to_id'] : null,
                'replyToName' => isset($m['reply_to_name']) ? $m['reply_to_name'] : null,
                'replyToMsg' => isset($m['reply_to_msg']) ? $m['reply_to_msg'] : null
            ];
        }
        
        // Sync response
        respond(true, [
            'sync' => [
                'roomId' => (int)$room['id'],
                'videoUrl' => $room['video_url'] ?: '',
                'videoTitle' => $room['video_title'] ?: '',
                'isPlaying' => (int)$room['is_playing'] === 1,
                'playbackTime' => (float)$room['playback_time'],
                'lastSyncTime' => (int)$room['last_sync_time'],
                'ownerId' => (int)$room['owner_id'],
                'myRole' => $role,
                'myMuteStatus' => $isMuted,
                'participants' => $participants,
                'newMessages' => $new_messages
            ]
        ]);
        break;

    case 'room_chat':
        $userId = (int)($input['userId'] ?? 0);
        $roomId = (int)($input['roomId'] ?? 0);
        $message = trim($input['message'] ?? '');
        
        if ($userId <= 0 || $roomId <= 0 || empty($message)) {
            respond(false, [], "Geçersiz sohbet verisi.");
        }
        
        // Check if muted
        $stmt_m = $db->prepare("SELECT is_muted FROM room_participants WHERE room_id = :rid AND user_id = :uid");
        $stmt_m->execute([':rid' => $roomId, ':uid' => $userId]);
        $part = $stmt_m->fetch(PDO::FETCH_ASSOC);
        
        if (!$part) respond(false, [], "Odada değilsiniz.");
        if ((int)$part['is_muted'] === 1) {
            respond(false, [], "Sessize alındınız (Sohbet edemezsiniz).");
        }
        
        $replyToId = isset($input['replyToId']) ? (int)$input['replyToId'] : null;
        $replyToName = isset($input['replyToName']) ? trim($input['replyToName']) : null;
        $replyToMsg = isset($input['replyToMsg']) ? trim($input['replyToMsg']) : null;

        $stmt = $db->prepare("INSERT INTO room_messages (room_id, user_id, message, is_system, timestamp, reply_to_id, reply_to_name, reply_to_msg) VALUES (:rid, :uid, :msg, 0, :ts, :rtid, :rtname, :rtmsg)");
        $stmt->execute([
            ':rid' => $roomId,
            ':uid' => $userId,
            ':msg' => $message,
            ':ts' => time(),
            ':rtid' => $replyToId,
            ':rtname' => $replyToName,
            ':rtmsg' => $replyToMsg
        ]);
        
        respond(true, ['messageId' => (int)$db->lastInsertId()]);
        break;

    case 'delete_room_message':
        $userId = (int)($input['userId'] ?? 0);
        $roomId = (int)($input['roomId'] ?? 0);
        $messageId = (int)($input['messageId'] ?? 0);
        
        if ($userId <= 0 || $roomId <= 0 || $messageId <= 0) {
            respond(false, [], "Geçersiz silme verisi.");
        }
        
        // Fetch message to verify owner or authority
        $stmt_msg = $db->prepare("SELECT * FROM room_messages WHERE id = :mid AND room_id = :rid");
        $stmt_msg->execute([':mid' => $messageId, ':rid' => $roomId]);
        $msg = $stmt_msg->fetch(PDO::FETCH_ASSOC);
        
        if (!$msg) {
            respond(false, [], "Mesaj bulunamadı.");
        }
        
        // Fetch current user's role in the room to check if they are owner or moderator
        $stmt_my = $db->prepare("SELECT role FROM room_participants WHERE room_id = :rid AND user_id = :uid");
        $stmt_my->execute([':rid' => $roomId, ':uid' => $userId]);
        $my_role = $stmt_my->fetchColumn();
        
        $is_owner_or_mod = ($my_role === 'owner' || $my_role === 'moderator');
        $is_message_author = ((int)$msg['user_id'] === $userId);
        
        if (!$is_message_author && !$is_owner_or_mod) {
            respond(false, [], "Bu mesajı silme yetkiniz yok.");
        }
        
        // Set message as deleted in databases
        $upd = $db->prepare("UPDATE room_messages SET is_deleted = 1, message = 'Bu mesaj silindi.' WHERE id = :mid");
        $upd->execute([':mid' => $messageId]);
        
        respond(true, ['message' => 'Mesaj silindi.']);
        break;

    case 'room_moderate':
        $userId = (int)($input['userId'] ?? 0);
        $roomId = (int)($input['roomId'] ?? 0);
        $targetId = (int)($input['targetId'] ?? 0);
        $command = $input['command'] ?? ''; // kick, mute, unmute, promote, demote
        
        if ($userId <= 0 || $roomId <= 0 || $targetId <= 0 || empty($command)) {
            respond(false, [], "Geçersiz moderasyon verisi.");
        }
        
        // Check if user has moderation authority (owner or moderator)
        $stmt_my = $db->prepare("SELECT role FROM room_participants WHERE room_id = :rid AND user_id = :uid");
        $stmt_my->execute([':rid' => $roomId, ':uid' => $userId]);
        $my_role = $stmt_my->fetchColumn();
        
        if ($my_role !== 'owner' && $my_role !== 'moderator') {
            respond(false, [], "Yetersiz yetki (Sadece oda sahibi veya moderatörler yapabilir).");
        }
        
        // Fetch target role
        $stmt_t = $db->prepare("SELECT role FROM room_participants WHERE room_id = :rid AND user_id = :uid");
        $stmt_t->execute([':rid' => $roomId, ':uid' => $targetId]);
        $target_role = $stmt_t->fetchColumn();
        
        if (!$target_role) {
            respond(false, [], "Hedef kullanıcı odada bulunamadı.");
        }
        
        // Prevent moderating higher/equal power levels
        if ($my_role === 'moderator' && ($target_role === 'owner' || $target_role === 'moderator')) {
            respond(false, [], "Moderatör yetkisiyle diğer yetkililere işlem yapamazsınız.");
        }
        
        // Ensure never self moderate
        if ($userId === $targetId) {
            respond(false, [], "Kendinize işlem yapamazsınız.");
        }
        
        // Get target username
        $stmt_u_name = $db->prepare("SELECT username FROM users WHERE id = :id");
        $stmt_u_name->execute([':id' => $targetId]);
        $target_username = $stmt_u_name->fetchColumn() ?: 'Kullanıcı';
        
        $success_msg = "";
        $sys_msg = "";
        
        if ($command === 'kick') {
            // Kick user
            $del = $db->prepare("DELETE FROM room_participants WHERE room_id = :rid AND user_id = :uid");
            $del->execute([':rid' => $roomId, ':uid' => $targetId]);
            
            // Prevent fast join immediately (add to kicked)
            $ins_k = $db->prepare("INSERT OR REPLACE INTO kicked_users (room_id, user_id, kicked_at) VALUES (:rid, :uid, :now)");
            $ins_k->execute([':rid' => $roomId, ':uid' => $targetId, ':now' => time()]);
            
            $success_msg = "Kullanıcı odadan atıldı.";
            $sys_msg = "system_kick:" . $target_username;
            
        } else if ($command === 'mute') {
            $upd = $db->prepare("UPDATE room_participants SET is_muted = 1 WHERE room_id = :rid AND user_id = :uid");
            $upd->execute([':rid' => $roomId, ':uid' => $targetId]);
            $success_msg = "Kullanıcı sessize alındı.";
            $sys_msg = "system_mute:" . $target_username;
            
        } else if ($command === 'unmute') {
            $upd = $db->prepare("UPDATE room_participants SET is_muted = 0 WHERE room_id = :rid AND user_id = :uid");
            $upd->execute([':rid' => $roomId, ':uid' => $targetId]);
            $success_msg = "Kullanıcı konuşma engeli kaldırıldı.";
            $sys_msg = "system_unmute:" . $target_username;
            
        } else if ($command === 'promote') {
            if ($my_role !== 'owner') respond(false, [], "Sadece oda kurucusu moderatör atayabilir.");
            $upd = $db->prepare("UPDATE room_participants SET role = 'moderator' WHERE room_id = :rid AND user_id = :uid");
            $upd->execute([':rid' => $roomId, ':uid' => $targetId]);
            $success_msg = "Moderatör yetkisi verildi.";
            $sys_msg = "system_promote:" . $target_username;
            
        } else if ($command === 'demote') {
            if ($my_role !== 'owner') respond(false, [], "Sadece oda kurucusu moderatörlük yetkisi alabilir.");
            $upd = $db->prepare("UPDATE room_participants SET role = 'member' WHERE room_id = :rid AND user_id = :uid");
            $upd->execute([':rid' => $roomId, ':uid' => $targetId]);
            $success_msg = "Kullanıcının moderatör yetkisi kaldırıldı.";
            $sys_msg = "system_demote:" . $target_username;
        }
        
        // Insert system message about action
        if (!empty($sys_msg)) {
            $sys_stmt = $db->prepare("INSERT INTO room_messages (room_id, user_id, message, is_system, timestamp) VALUES (:rid, NULL, :msg, 1, :ts)");
            $sys_stmt->execute([
                ':rid' => $roomId,
                ':msg' => $sys_msg,
                ':ts' => time()
            ]);
        }
        
        respond(true, ['message' => $success_msg]);
        break;

    case 'get_friends':
        $userId = (int)($input['userId'] ?? 0);
        if ($userId <= 0) respond(false, [], "Yetkisiz erişim.");
        
        // Friends query: Status accepted
        $stmt = $db->prepare("
            SELECT u.id, u.username, u.avatar, u.status_text
            FROM friends f
            JOIN users u ON (f.friend_id = u.id AND f.user_id = :u) OR (f.user_id = u.id AND f.friend_id = :u)
            WHERE f.status = 'accepted'
        ");
        $stmt->execute([':u' => $userId]);
        $friends = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        // Incoming pending requests: WHERE friend_id = my_id AND status = pending
        $stmt_inc = $db->prepare("
            SELECT u.id, u.username, u.avatar, u.status_text
            FROM friends f
            JOIN users u ON f.user_id = u.id
            WHERE f.friend_id = :u AND f.status = 'pending'
        ");
        $stmt_inc->execute([':u' => $userId]);
        $incomingRequests = $stmt_inc->fetchAll(PDO::FETCH_ASSOC);
        
        // Outgoing pending requests: WHERE user_id = my_id AND status = pending
        $stmt_out = $db->prepare("
            SELECT u.id, u.username, u.avatar, u.status_text
            FROM friends f
            JOIN users u ON f.friend_id = u.id
            WHERE f.user_id = :u AND f.status = 'pending'
        ");
        $stmt_out->execute([':u' => $userId]);
        $outgoingRequests = $stmt_out->fetchAll(PDO::FETCH_ASSOC);
        
        // Format lists correctly
        $f_list = [];
        foreach ($friends as $item) {
            $f_list[] = [
                'userId' => (int)$item['id'],
                'username' => $item['username'],
                'avatar' => $item['avatar'],
                'statusText' => $item['status_text']
            ];
        }
        
        $inc_list = [];
        foreach ($incomingRequests as $item) {
            $inc_list[] = [
                'userId' => (int)$item['id'],
                'username' => $item['username'],
                'avatar' => $item['avatar'],
                'statusText' => $item['status_text']
            ];
        }
        
        $out_list = [];
        foreach ($outgoingRequests as $item) {
            $out_list[] = [
                'userId' => (int)$item['id'],
                'username' => $item['username'],
                'avatar' => $item['avatar'],
                'statusText' => $item['status_text']
            ];
        }
        
        respond(true, [
            'friends' => $f_list,
            'incomingRequests' => $inc_list,
            'outgoingRequests' => $out_list
        ]);
        break;

    case 'friend_action':
        $userId = (int)($input['userId'] ?? 0);
        $targetUsername = trim($input['targetUsername'] ?? '');
        $actionType = $input['action'] ?? ''; // send_request, accept, reject, remove
        
        if ($userId <= 0 || empty($targetUsername) || empty($actionType)) {
            respond(false, [], "Geçersiz arkadaşlık isteği verisi.");
        }
        
        // Retrieve target user profile
        $stmt_t = $db->prepare("SELECT id FROM users WHERE username = :u");
        $stmt_t->execute([':u' => $targetUsername]);
        $target = $stmt_t->fetch(PDO::FETCH_ASSOC);
        
        if (!$target) {
            respond(false, [], "Belirtilen kullanıcı bulunamadı.");
        }
        
        $targetId = (int)$target['id'];
        
        if ($userId === $targetId) {
            respond(false, [], "Kendinize istek gönderemezsiniz.");
        }
        
        if ($actionType === 'send_request') {
            // Check existing relationship
            $stmt_chk = $db->prepare("SELECT status, user_id FROM friends WHERE (user_id = :u AND friend_id = :t) OR (user_id = :t AND friend_id = :u)");
            $stmt_chk->execute([':u' => $userId, ':t' => $targetId]);
            $rel = $stmt_chk->fetch(PDO::FETCH_ASSOC);
            
            if ($rel) {
                if ($rel['status'] === 'accepted') {
                    respond(false, [], "Zaten arkadaşsınız.");
                } else {
                    respond(false, [], "Sırada bekleyen bir istek bulunuyor.");
                }
            }
            
            // Insert friend request: user_id sends to friend_id (user_id is sender, friend_id is receiver)
            $ins = $db->prepare("INSERT INTO friends (user_id, friend_id, status) VALUES (:u, :f, 'pending')");
            $ins->execute([':u' => $userId, ':f' => $targetId]);
            
            respond(true, ['message' => 'Arkadaşlık isteği gönderildi']);
            
        } else if ($actionType === 'accept') {
            // Accept. This means targetId is sender, userId (me) is receiver
            $upd = $db->prepare("UPDATE friends SET status = 'accepted' WHERE user_id = :t AND friend_id = :u");
            $upd->execute([':t' => $targetId, ':u' => $userId]);
            
            if ($upd->rowCount() > 0) {
                respond(true, ['message' => 'Arkadaşlık isteği kabul edildi']);
            } else {
                respond(false, [], "Kabul edilecek bekleyen istek bulunamadı.");
            }
            
        } else if ($actionType === 'reject') {
            // Delete request. Either direction can delete.
            $del = $db->prepare("DELETE FROM friends WHERE (user_id = :t AND friend_id = :u) OR (user_id = :u AND friend_id = :t)");
            $del->execute([':t' => $targetId, ':u' => $userId]);
            respond(true, ['message' => 'İstek reddedildi veya silindi']);
            
        } else if ($actionType === 'remove') {
            $del = $db->prepare("DELETE FROM friends WHERE (user_id = :u AND friend_id = :t) OR (user_id = :t AND friend_id = :u)");
            $del->execute([':u' => $userId, ':t' => $targetId]);
            respond(true, ['message' => 'Arkadaşlıktan çıkarıldı']);
        }
        break;

    case 'get_dms':
        $userId = (int)($input['userId'] ?? 0);
        if ($userId <= 0) respond(false, [], "Yetkisiz erişim.");
        
        // List active conversations. Find all friends we chatted with or friends in relations to easily trigger chats.
        // To be smart, we list unique users with whom we have direct messages.
        $stmt = $db->prepare("
            SELECT DISTINCT partner_id, u.username, u.avatar, u.status_text,
            (SELECT message FROM direct_messages WHERE (sender_id = :u AND receiver_id = partner_id) OR (sender_id = partner_id AND receiver_id = :u) ORDER BY id DESC LIMIT 1) as last_msg,
            (SELECT timestamp FROM direct_messages WHERE (sender_id = :u AND receiver_id = partner_id) OR (sender_id = partner_id AND receiver_id = :u) ORDER BY id DESC LIMIT 1) as last_ts,
            (SELECT COUNT(*) FROM direct_messages WHERE sender_id = partner_id AND receiver_id = :u AND is_read = 0) as unread_count
            FROM (
                SELECT DISTINCT receiver_id as partner_id FROM direct_messages WHERE sender_id = :u
                UNION
                SELECT DISTINCT sender_id as partner_id FROM direct_messages WHERE receiver_id = :u
            ) as conversations
            JOIN users u ON conversations.partner_id = u.id
            ORDER BY last_ts DESC
        ");
        $stmt->execute([':u' => $userId]);
        $conversations = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        $out = [];
        foreach ($conversations as $c) {
            $out[] = [
                'userId' => (int)$c['partner_id'],
                'username' => $c['username'],
                'avatar' => $c['avatar'],
                'statusText' => $c['status_text'],
                'lastMessage' => $c['last_msg'] ?: '',
                'unreadCount' => (int)$c['unread_count'],
                'lastMessageTimestamp' => (int)$c['last_ts']
            ];
        }
        
        respond(true, ['conversations' => $out]);
        break;

    case 'get_dm_messages':
        $userId = (int)($input['userId'] ?? 0);
        $partnerId = (int)($input['partnerId'] ?? 0);
        
        if ($userId <= 0 || $partnerId <= 0) respond(false, [], "Geçersiz sohbet tarafları.");
        
        // Mark as read
        $upd = $db->prepare("UPDATE direct_messages SET is_read = 1 WHERE sender_id = :p AND receiver_id = :u");
        $upd->execute([':p' => $partnerId, ':u' => $userId]);
        
        // Fetch last 50 dm messages
        $stmt = $db->prepare("
            SELECT dm.*, s.username as sender_name, s.avatar as sender_avatar
            FROM direct_messages dm
            JOIN users s ON dm.sender_id = s.id
            WHERE (sender_id = :u AND receiver_id = :p) OR (sender_id = :p AND receiver_id = :u)
            ORDER BY dm.id ASC LIMIT 50
        ");
        $stmt->execute([':u' => $userId, ':p' => $partnerId]);
        $messages_raw = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        $messages = [];
        foreach ($messages_raw as $m) {
            $messages[] = [
                'id' => (int)$m['id'],
                'senderId' => (int)$m['sender_id'],
                'receiverId' => (int)$m['receiver_id'],
                'senderName' => $m['sender_name'],
                'senderAvatar' => $m['sender_avatar'],
                'message' => $m['message'],
                'timestamp' => (int)$m['timestamp'],
                'isRead' => (int)$m['is_read'] === 1
            ];
        }
        
        respond(true, ['messages' => $messages]);
        break;

    case 'send_dm':
        $userId = (int)($input['userId'] ?? 0);
        $receiverId = (int)($input['receiverId'] ?? 0);
        $message = trim($input['message'] ?? '');
        
        if ($userId <= 0 || $receiverId <= 0 || empty($message)) {
            respond(false, [], "Mesaj içeriği boş olamaz.");
        }
        
        $ins = $db->prepare("INSERT INTO direct_messages (sender_id, receiver_id, message, timestamp, is_read) VALUES (:s, :r, :m, :t, 0)");
        $ins->execute([
            ':s' => $userId,
            ':r' => $receiverId,
            ':m' => $message,
            ':t' => time()
        ]);
        
        // Also ensure they are tracked as friends or last contact.
        respond(true, ['messageId' => (int)$db->lastInsertId()]);
        break;

    case 'poll_notifications':
        $userId = (int)($input['userId'] ?? 0);
        $lastCheckedId = (int)($input['lastCheckedId'] ?? 0);
        
        if ($userId <= 0) respond(false, [], "Geçersiz yetki.");
        
        // Find unread direct messages newer than lastCheckedId for notification triggering
        $stmt = $db->prepare("
            SELECT dm.*, u.username as sender_name, u.avatar as sender_avatar
            FROM direct_messages dm
            JOIN users u ON dm.sender_id = u.id
            WHERE dm.receiver_id = :u AND dm.is_read = 0 AND dm.id > :lci
            ORDER BY dm.id ASC
        ");
        $stmt->execute([':u' => $userId, ':lci' => $lastCheckedId]);
        $new_dms = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        $out = [];
        foreach ($new_dms as $m) {
            $out[] = [
                'id' => (int)$m['id'],
                'senderId' => (int)$m['sender_id'],
                'senderName' => $m['sender_name'],
                'senderAvatar' => $m['sender_avatar'],
                'message' => $m['message'],
                'timestamp' => (int)$m['timestamp']
            ];
        }
        
        respond(true, ['newDMs' => $out]);
        break;

    default:
        respond(false, [], "Bilinmeyen API işlemi: " . $action);
        break;
}
