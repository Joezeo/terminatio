mod imp;

use core::{to_credential_type_const, CredentialType, SessionCredential};

use glib::Object;
use gtk::glib;
use gtk::subclass::prelude::*;

glib::wrapper! {
    pub struct SessionCredentialObject(ObjectSubclass<imp::SessionCredentialObject>);
}

impl SessionCredentialObject {
    pub fn new(
        shown_name: &str,
        host: &str,
        user: &str,
        password: &str,
        group: &str,
        port: i32,
        credential_type: CredentialType,
    ) -> Self {
        Object::builder()
            .property("shown-name", shown_name)
            .property("host", host)
            .property("user", user)
            .property("password", password)
            .property("group", group)
            .property("port", port)
            .property(
                "credential-type",
                to_credential_type_const(&credential_type),
            )
            .build()
    }

    pub fn from_session_credential(session_credential: SessionCredential) -> Self {
        SessionCredentialObject::new(
            &session_credential.shown_name,
            &session_credential.host,
            &session_credential.user,
            &session_credential.password,
            &session_credential.group,
            session_credential.port,
            session_credential.credential_type,
        )
    }

    pub fn group(&self) -> String {
        self.imp().group.borrow().clone()
    }

    pub fn shown_name(&self) -> String {
        self.imp().shown_name.borrow().clone()
    }

    pub fn to_session_credetial(&self) -> SessionCredential {
        let obj = self.imp();
        SessionCredential::new(
            obj.shown_name.borrow().clone(),
            obj.host.borrow().clone(),
            obj.user.borrow().clone(),
            obj.password.borrow().clone(),
            obj.group.borrow().clone(),
            obj.port.get(),
            obj.credential_type
                .get()
                .expect("`credential_type` should initialize first before use.")
                .clone(),
        )
    }
    pub fn to_shown_string(&self) -> String {
        let mut shown_string = String::new();
        shown_string.push_str(&self.shown_name());
        shown_string
    }
}
