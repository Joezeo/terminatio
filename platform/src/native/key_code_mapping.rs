use gtk::gdk::{ffi::*, ModifierType};
use log::debug;

pub struct QtCodeMapping;

#[allow(non_upper_case_globals)]
impl QtCodeMapping {
    pub fn get_qt_modifier(gtk_modifier: ModifierType) -> i32 {
        match gtk_modifier {
            ModifierType::SHIFT_MASK => 0,
            ModifierType::LOCK_MASK => 0,
            ModifierType::CONTROL_MASK => 0,
            ModifierType::ALT_MASK => 0,
            ModifierType::BUTTON1_MASK => 0,
            ModifierType::BUTTON2_MASK => 0,
            ModifierType::BUTTON3_MASK => 0,
            ModifierType::BUTTON4_MASK => 0,
            ModifierType::BUTTON5_MASK => 0,
            ModifierType::SUPER_MASK => 0,
            ModifierType::HYPER_MASK => 0,
            ModifierType::META_MASK => 0,
            _ => {
                debug!("Non match modfier.");
                0
            }
        }
    }

    pub fn get_qt_code(gtk_code: i32) -> i32 {
        match gtk_code {
            GDK_KEY_Escape => 0x01000000,
            GDK_KEY_Tab => 0x01000001,
            GDK_KEY_BackSpace => 0x01000003,
            GDK_KEY_KP_Enter => 0x01000005,
            GDK_KEY_ISO_Enter => 0x01000005,
            GDK_KEY_3270_Enter => 0x01000005,
            GDK_KEY_Insert => 0x01000006,
            GDK_KEY_KP_Insert => 0x01000006,
            GDK_KEY_Delete => 0x01000007,
            GDK_KEY_KP_Delete => 0x01000007,
            GDK_KEY_Pause => 0x01000008,
            GDK_KEY_Print => 0x01000009,
            GDK_KEY_Clear => 0x0100000b,
            GDK_KEY_Home => 0x01000010,
            GDK_KEY_KP_Home => 0x01000010,
            GDK_KEY_End => 0x01000011,
            GDK_KEY_Left => 0x01000012,
            GDK_KEY_leftarrow => 0x01000012,
            GDK_KEY_Up => 0x01000013,
            GDK_KEY_uparrow => 0x01000013,
            GDK_KEY_Right => 0x01000014,
            GDK_KEY_rightarrow => 0x01000014,
            GDK_KEY_Down => 0x01000015,
            GDK_KEY_downarrow => 0x01000015,
            GDK_KEY_Page_Up => 0x01000016,
            GDK_KEY_KP_Page_Up => 0x01000016,
            GDK_KEY_Page_Down => 0x01000017,
            GDK_KEY_KP_Page_Down => 0x01000017,
            GDK_KEY_Shift_L => 0x01000020,
            GDK_KEY_Shift_R => 0x01000020,
            GDK_KEY_Control_L => 0x01000021,
            GDK_KEY_Control_R => 0x01000021,
            GDK_KEY_Meta_L => 0x01000022,
            GDK_KEY_Meta_R => 0x01000022,
            GDK_KEY_Alt_L => 0x01000023,
            GDK_KEY_Alt_R => 0x01000023,
            GDK_KEY_Caps_Lock => 0x01000024,
            GDK_KEY_Num_Lock => 0x01000025,
            GDK_KEY_Scroll_Lock => 0x01000026,
            GDK_KEY_F1 => 0x01000030,
            GDK_KEY_F2 => 0x01000031,
            GDK_KEY_F3 => 0x01000032,
            GDK_KEY_F4 => 0x01000033,
            GDK_KEY_F5 => 0x01000034,
            GDK_KEY_F6 => 0x01000035,
            GDK_KEY_F7 => 0x01000036,
            GDK_KEY_F8 => 0x01000037,
            GDK_KEY_F9 => 0x01000038,
            GDK_KEY_F10 => 0x01000039,
            GDK_KEY_F11 => 0x0100003a,
            GDK_KEY_F12 => 0x0100003b,
            GDK_KEY_F13 => 0x0100003c,
            GDK_KEY_F14 => 0x0100003d,
            GDK_KEY_F15 => 0x0100003e,
            GDK_KEY_F16 => 0x0100003f,
            GDK_KEY_F17 => 0x01000040,
            GDK_KEY_F18 => 0x01000041,
            GDK_KEY_F19 => 0x01000042,
            GDK_KEY_F20 => 0x01000043,
            GDK_KEY_F21 => 0x01000044,
            GDK_KEY_F22 => 0x01000045,
            GDK_KEY_F23 => 0x01000046,
            GDK_KEY_F24 => 0x01000047,
            _ => {
                debug!("Non match key code.");
                gtk_code
            },
        }
    }
}
