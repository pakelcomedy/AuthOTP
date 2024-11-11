```
com/
└── pakelcomedy/
    └── otpauth/
        ├── MainActivity.kt
        ├── ui/
        │   ├── SignInFragment.kt
        │   ├── SignUpFragment.kt
        │   ├── PasswordFragment.kt
        │   ├── OtpVerificationFragment.kt
        │   └── HomeFragment.kt
        ├── viewmodel/
        │   ├── AuthViewModel.kt
        │   ├── SignUpViewModel.kt
        │   ├── PasswordViewModel.kt
        │   ├── OtpVerificationViewModel.kt
        │   └── HomeViewModel.kt
        └── model/
        │   ├── User.kt
        │   ├── OtpRequest.kt
        │   └── UpdatePasswordRequest.kt
        └── network/
        │   ├── ApiClient.kt
        │   └── ApiService.kt
        └── utils/
            └── EmailSender.kt

res/
├── layout/
│   ├── activity_main.xml                # Layout for MainActivity
│   ├── fragment_sign_in.xml             # Layout for SignInFragment
│   ├── fragment_sign_up.xml             # Layout for SignUpFragment
│   ├── fragment_password.xml             # Layout for SignUpFragment
│   ├── fragment_otp_verification.xml    # Layout for OtpVerificationFragment
│   └── fragment_home.xml                # Layout for HomeFragment
├── values/
│   ├── colors.xml                       # Define app colors
│   ├── strings.xml                      # Define app strings
│   ├── styles.xml                       # Define app theme styles
│   └── dimens.xml                       # Define app dimensions (padding, margins, etc.)
├── navigation/
│   └── nav_graph.xml                    # Navigation graph for fragment transitions
└── drawable/
    ├── ic_launcher_background.xml       # App icon background
    └── ic_launcher_foreground.xml       # App icon foreground
```
