#!/usr/bin/env python3
"""
Dice Sound Generator - Công cụ tinh chỉnh âm thanh xúc xắc
Chạy lệnh sau tại thư mục gốc của project để sinh lại file âm thanh:
    python3 scripts/generate_dice_sound.py
"""

import math
import struct
import random
import wave
import os

# ==============================================================================
# BẢNG THÔNG SỐ TÙY CHỈNH (CONFIG)
# ==============================================================================

# 1. ĐỘ TRẦM VÀ LỰC ĐẬP "BỊCH" (THUD)
# Càng nhỏ thì tiếng đập "bịch" càng sâu và nặng
THUD_FREQ_BASE = 128.0    # Tần số lực đập bịch (Gợi ý: 100 - 160 Hz). Mặc định 128 Hz (rất trầm, chắc)
BODY_FREQ_BASE = 370.0    # Tần số thân khối cộp (Gợi ý: 300 - 450 Hz)

# 2. ĐỘ SẮC NÉT TIẾNG GÕ "CẠCH"
# Tỉ lệ tiếng cạch gỗ mờ trên nền tiếng bịch (0.0 = chỉ có tiếng bịch, 0.4 = vừa phải, 0.8 = rõ cạch hơn)
CLICK_WEIGHT = 0.35

# 3. THỜI LƯỢNG VÀ TỔNG THỂ
SAMPLE_RATE = 44100
TOTAL_DURATION_SEC = 0.50  # 500ms dứt khoát, gọn gàng, không bị tưng tưng

# Đường dẫn file xuất ra (tự động lưu vào thư mục res/raw của app)
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
OUTPUT_WAV_PATH = os.path.abspath(os.path.join(SCRIPT_DIR, "../app/src/main/res/raw/dice_roll.wav"))

# ==============================================================================
# HÀM XỬ LÝ VẬT LÝ VÀ SINH ÂM THANH
# ==============================================================================

def create_heavy_thud_clack(sample_rate, amp=1.0, click_w=0.35, thud_f=128.0, body_f=370.0):
    dur = 0.045
    num_samples = int(sample_rate * dur)
    out = [0.0] * num_samples
    
    p_thud = random.uniform(0, 6.28)
    p_body = random.uniform(0, 6.28)
    lp_state = 0.0
    
    for i in range(num_samples):
        t = i / sample_rate
        
        # 1. Lực đập "BỊCH" trầm sâu (110 - 150 Hz)
        env_thud = math.exp(-t / 0.012)
        freq_cur = thud_f * (1.0 + 0.3 * math.exp(-t / 0.004))
        thud = math.sin(2 * math.pi * freq_cur * t + p_thud) * env_thud
        
        # 2. Khối đầm "CỘP" (320 - 420 Hz)
        env_body = math.exp(-t / 0.008)
        body = math.sin(2 * math.pi * body_f * t + p_body) * env_body
        
        # 3. Va chạm "CẠCH" dứt khoát không chói
        env_click = math.exp(-t / 0.0022)
        raw_noise = (random.random() * 2.0 - 1.0)
        lp_state += 0.18 * (raw_noise - lp_state)
        click = lp_state * env_click
        
        sample = (thud * 0.75 + body * 0.45 + click * click_w) * amp
        out[i] = sample
        
    return out

def generate_dice_roll():
    random.seed(303)
    total_samples = int(SAMPLE_RATE * TOTAL_DURATION_SEC)
    buffer = [0.0] * total_samples
    
    impacts = [
        # (thời_điểm, biên_độ, click_w, thud_freq, body_freq)
        (0.000, 1.00, CLICK_WEIGHT * 1.2, THUD_FREQ_BASE * 0.98, BODY_FREQ_BASE * 0.97),
        (0.045, 0.70, CLICK_WEIGHT * 1.0, THUD_FREQ_BASE * 1.05, BODY_FREQ_BASE * 1.05),
        (0.100, 0.78, CLICK_WEIGHT * 1.1, THUD_FREQ_BASE * 1.00, BODY_FREQ_BASE * 1.00),
        (0.165, 0.55, CLICK_WEIGHT * 0.9, THUD_FREQ_BASE * 1.08, BODY_FREQ_BASE * 1.10),
        (0.240, 0.40, CLICK_WEIGHT * 0.8, THUD_FREQ_BASE * 1.02, BODY_FREQ_BASE * 1.02),
        (0.320, 0.25, CLICK_WEIGHT * 0.7, THUD_FREQ_BASE * 1.12, BODY_FREQ_BASE * 1.12),
        (0.390, 0.15, CLICK_WEIGHT * 0.6, THUD_FREQ_BASE * 1.05, BODY_FREQ_BASE * 1.05),
    ]
    
    for t_start, amp, cw, tf, bf in impacts:
        start_idx = int(t_start * SAMPLE_RATE)
        clack = create_heavy_thud_clack(SAMPLE_RATE, amp=amp, click_w=cw, thud_f=tf, body_f=bf)
        for i, val in enumerate(clack):
            if start_idx + i < total_samples:
                buffer[start_idx + i] += val
                
    # Nén ấm âm thanh (Saturation / Warm Compression)
    max_val = max(abs(x) for x in buffer)
    if max_val > 0:
        buffer = [x / max_val for x in buffer]
        buffer = [math.tanh(x * 1.35) for x in buffer]
        peak = max(abs(x) for x in buffer)
        buffer = [x * (0.92 / peak) for x in buffer]
        
    os.makedirs(os.path.dirname(OUTPUT_WAV_PATH), exist_ok=True)
    with wave.open(OUTPUT_WAV_PATH, 'wb') as wav_file:
        wav_file.setnchannels(1)
        wav_file.setsampwidth(2)
        wav_file.setframerate(SAMPLE_RATE)
        frames = bytearray()
        for s in buffer:
            val = int(max(min(s, 1.0), -1.0) * 32767)
            frames.extend(struct.pack('<h', val))
        wav_file.writeframes(frames)
        
    print(f"[OK] Đã tạo thành công file âm thanh tại:")
    print(f"     -> {OUTPUT_WAV_PATH}")
    print(f"     (Thud: {THUD_FREQ_BASE}Hz, Body: {BODY_FREQ_BASE}Hz, ClickWeight: {CLICK_WEIGHT})")

if __name__ == "__main__":
    generate_dice_roll()
