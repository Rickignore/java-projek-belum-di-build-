rule suspicious_string_example
{
    meta:
        author = "Anda"
        date = "2025-10-08"
        description = "Contoh rule mencari string mencurigakan"

    strings:
        $s1 = "malicious_function"
        $s2 = { 6A 40 68 ?? ?? ?? ?? 6A 00 }

    condition:
        any of ($s*) and filesize < 10MB
}
