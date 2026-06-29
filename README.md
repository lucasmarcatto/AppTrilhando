# 🥾 Trilhando

Aplicativo Android de registro de caminhadas — uma espécie de diário/Strava simplificado.
O usuário cria uma conta, inicia uma caminhada, acompanha os passos e a localização em
tempo real, tira uma foto, finaliza descrevendo o percurso (por voz ou texto) e consulta
o histórico das caminhadas salvas.

---

## ✨ Funcionalidades

- **Autenticação** de usuários com e-mail e senha (cadastro, login, auto-login e logout).
- **Caminhada em tempo real**: contagem de passos pelo sensor do aparelho e captura de
  localização (latitude/longitude) via GPS.
- **Foto da caminhada** usando a câmera do dispositivo.
- **Descrição por voz**: reconhecimento de fala converte o áudio em texto na tela de
  finalização (também é possível digitar).
- **Persistência na nuvem**: caminhadas e perfis salvos no Cloud Firestore.
- **Histórico** de caminhadas do usuário, com tela de detalhes.
- **Notificações** de lembrete e feedback visual via Toasts.

---

## 🛠️ Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Kotlin |
| UI | Android Views (XML) + AppCompat / Material |
| Autenticação | Firebase Authentication |
| Banco de dados | Cloud Firestore |
| Localização | Google Play Services — Fused Location Provider |
| Sensores | `SensorManager` (`TYPE_STEP_COUNTER`) |
| Voz | `SpeechRecognizer` (voz → texto) |
| Imagens | Câmera + armazenamento em Base64; carregamento com Glide |
| Build | Gradle (Kotlin DSL) |

**Requisitos de SDK**

- `minSdk` 33 (Android 13)
- `targetSdk` / `compileSdk` 35
- Java 11

---

## 🏗️ Arquitetura

O projeto adota **baixo acoplamento**: as telas (`Activities`) apenas orquestram, enquanto
a lógica de recursos do aparelho fica em **Helpers** e o acesso ao banco em **Repositories**.
A comunicação acontece por *callbacks*, então trocar a implementação de um recurso não quebra
o restante do app.

```
com.trilhando
├── auth          → FirebaseAuthHelper (login/cadastro/sessão)
├── helper        → StepCounterHelper, LocationHelper, PermissionHelper,
│                   SpeechHelper, CameraHelper, Base64Helper,
│                   NotificationHelper, AlarmScheduler
├── model         → Usuario, Caminhada
├── repository    → UserRepository, WalkRepository (Firestore)
├── adapter       → WalkAdapter (RecyclerView do histórico)
├── receiver      → NotificationReceiver
└── ui
    ├── login      → LoginActivity
    ├── register   → RegisterActivity
    ├── home       → HomeActivity
    ├── walk       → StartWalkActivity, FinishWalkActivity
    ├── camera     → CameraActivity
    ├── historico  → HistoryActivity
    ├── details    → WalkDetailsActivity
    └── settings   → SettingsActivity
```

### Fluxo da caminhada

1. **HomeActivity** — mostra o usuário e estatísticas; botão para iniciar a caminhada.
2. **StartWalkActivity** — inicia/para a contagem de passos e a localização, permite tirar
   foto. Ao finalizar, encaminha os dados coletados (passos, localização e foto) para a
   próxima tela.
3. **FinishWalkActivity** — exibe o resumo (passos e data), permite descrever a caminhada
   por voz ou texto e salva tudo no Firestore.
4. **HistoryActivity / WalkDetailsActivity** — lista e detalha as caminhadas salvas.

---

## 🚀 Como executar

### Pré-requisitos

- Android Studio (versão recente).
- Um projeto no [Firebase Console](https://console.firebase.google.com/) com
  **Authentication** (provedor E-mail/senha habilitado) e **Cloud Firestore** ativos.

### Passos

1. Clone o repositório e abra a pasta `AppTrilhando` no Android Studio.
2. Baixe o arquivo **`google-services.json`** do seu projeto Firebase e coloque-o em
   `app/google-services.json`.
3. No Firebase Console:
   - Em **Authentication → Método de login**, habilite **E-mail/senha**.
   - Em **Firestore Database**, crie o banco (modo de teste durante o desenvolvimento).
4. Sincronize o Gradle (*Sync Project with Gradle Files*).
5. Rode o app em um **dispositivo físico** (recomendado) ou emulador.

> ⚠️ **Sensor de passos** e **reconhecimento de voz** dependem de hardware real e
> normalmente **não funcionam em emulador**. Para testar essas funções, use um celular.

---

## 🔐 Permissões utilizadas

Declaradas em `AndroidManifest.xml` e solicitadas em tempo de execução:

- `INTERNET`
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` — localização
- `ACTIVITY_RECOGNITION` — sensor de passos
- `CAMERA` — foto da caminhada
- `RECORD_AUDIO` — descrição por voz
- `POST_NOTIFICATIONS` — lembretes
- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` — agendamento de notificações

---

## 🗄️ Estrutura de dados (Firestore)

| Coleção | Documento | Campos principais |
|---|---|---|
| `usuarios` | e-mail do usuário | `email`, `nome`, `fotoPerfil` |
| `caminhadas` | ID automático | `userId`, `titulo`, `descricao`, `latitude`, `longitude`, `quantidadePassos`, `fotoBase64`, `dataCriacao` |

As fotos são armazenadas como **string Base64** dentro do documento da caminhada.
---
## 🎥 Vídeos de Demonstração

### Demonstração curta


---

## 📄 Licença

Projeto acadêmico, desenvolvido para fins de estudo.
