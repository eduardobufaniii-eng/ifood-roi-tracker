# iFood ROI Tracker 📊🛵

App Android para entregadores iFood que calcula automaticamente se uma corrida vale a pena **antes de você aceitar**.

## 🎯 O que faz

| Funcionalidade | Descrição |
|----------------|-----------|
| **Detecção automática** | Lê a tela do iFood Entregador via Accessibility Service |
| **Cálculo em tempo real** | Valor total, R$/km, R$/h, distância, tempo estimado |
| **Decisão visual** | Overlay flutuante: ✅ VALE A PENA / ⚠️ AVALIAR / ❌ NÃO VALE |
| **Thresholds personalizáveis** | Mínimo R$/km, R$/h, distância máx, valor mínimo, horários de pico |
| **Histórico completo** | Todas as ofertas salvas localmente (SQLite/Room) |
| **Estatísticas** | Ganhos por bairro, hora, dia da semana, médias |
| **Export CSV** | Leve seus dados pro Excel/Sheets |
| **Backup/Restore** | Migre dados entre celulares |

---

## 📱 Requisitos

- **Android 8.0+** (API 26)
- **App iFood Entregador** instalado (`br.com.ifood.deliver`)
- Permissão **Acessibilidade** (obrigatório)
- Permissão **Exibir sobre outros apps** (obrigatório)

---

## 🚀 Instalação Rápida

### Opção 1: GitHub Actions (recomendado - sem PC)
1. Fork este repo
2. Vá em **Actions** → **Build APK** → **Run workflow**
3. Baixe o `app-debug.apk` nos artifacts
4. Instale no celular (permita "Instalar apps desconhecidos")

### Opção 2: Build Local (Android Studio)
```bash
# Clone
git clone https://github.com/seu-usuario/ifood-roi-tracker.git
cd ifood-roi-tracker

# Abra no Android Studio (Ladybug+)
# Build → Build Bundle(s) / APK(s) → Build APK(s)
```

### Opção 3: Linha de comando
```bash
./gradlew assembleDebug
# APK em: app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚙️ Configuração Inicial (30 seg)

1. **Abra o app** → toque em "Configurações" (⚙️)
2. **Ative Acessibilidade**:
   - Configurações → Acessibilidade → iFood ROI Tracker → **Ativar**
   - Permite ler a tela do iFood
3. **Ative Overlay**:
   - Configurações → Apps → Acesso especial → Exibir sobre outros apps → iFood ROI Tracker → **Permitir**
   - Mostra a bolha de decisão
4. **Ajuste thresholds** (ou use padrões):
   - R$/km mínimo: **4,00** (padrão SP/RJ/BH)
   - R$/h mínimo: **55,00**
   - Distância máx: **15 km**
   - Valor mínimo: **R$ 10,00**
   - Horários de pico: 11-14h e 18-21h (1.2x)

---

## 🎮 Como Usar

```
1. Fica online no iFood Entregador normalmente
2. Quando aparece "Nova corrida", o app lê a tela automaticamente
3. Bolha flutuante aparece com:
   ┌─────────────────────────────┐
   │ ✅ VALE A PENA              │
   │ 💰 Valor: R$ 18,50          │
   │ 📍 Distância: 4,2 km        │
   │ ⏱️ Tempo: 18 min            │
   │ 📊 R$/km: 4,40 (mín: 4,00)  │
   │ 🕐 R$/h: 61,67 (mín: 55,00) │
   │ 🏁 Origem: Shopping Center  │
   │ 🎯 Destino: Rua das Flores  │
   │ [✅ Aceitei] [❌ Recusei]    │
   └─────────────────────────────┘
4. Toque na decisão → bolha some → você aceita/recusa no iFood
5. Tudo salvo no histórico automaticamente
```

---

## 📊 Telas Principais

| Tela | Função |
|------|--------|
| **Início** | Status dos serviços, stats do dia, última oferta, thresholds rápidos |
| **Histórico** | Lista filtrável (Todas/Aceitas/Recusadas/Ignoradas), editar decisão |
| **Estatísticas** | Resumo + Por bairro + Por hora + Por dia da semana (Hoje/Semana/Mês/Tudo) |
| **Configurações** | Todos os thresholds, horários de pico, comportamento do overlay, backup |

---

## 🔧 Thresholds Padrão (Base SP/RJ/BH)

```kotlin
Thresholds(
    minValuePerKm = 4.00,      // Não aceita abaixo de R$ 4,00/km
    minValuePerHour = 55.00,   // Não aceita abaixo de R$ 55,00/h
    maxDistanceKm = 15,        // Não aceita acima de 15 km
    minTotalValue = 10.00,     // Não aceita abaixo de R$ 10,00 total
    peakMultiplier = 1.2,      // 20% mais exigente no pico
    peakHours = [
        PeakHour(11, 14),      // Almoço
        PeakHour(18, 21)       // Jantar
    ],
    autoDismissSeconds = 15,   // Some sozinho após 15s
    vibrateOnDecision = true,
    playSoundOnDecision = false,
    showOnLockscreen = true
)
```

> **Dica**: Ajuste conforme sua cidade. Em cidades menores, R$/km ≥ 3,50 pode ser melhor.

---

## 🛡️ Privacidade & Segurança

- **100% Local**: Nenhum dado sai do seu celular
- **Sem Internet**: App funciona totalmente offline
- **Só Lê Tela**: Não injeta toque, não automatiza aceitação
- **Open Source**: Código auditável
- **LGPD Compliant**: Seus dados, seu controle

---

## ⚠️ Limitações Conhecidas

| Limitação | Workaround |
|-----------|------------|
| Só Android | iOS não permite Accessibility Service |
| iFood muda layout | Parser usa heurísticas; se quebrar, avise que corrijo |
| WebView/Canvas | Se iFood migrar para Flutter/WebView, para de funcionar |
| Duas contas simultâneas | Use perfil de trabalho ou app cloner |

---

## 🐛 Troubleshooting

### "Acessibilidade não ativa"
- Vá em Configurações → Acessibilidade → iFood ROI Tracker → Force Stop → Ative novamente
- Alguns Xiaomi/Redmi: Configurações → Permissões → Acessibilidade → Ative "Serviços baixados"

### "Overlay não aparece"
- Configurações → Apps → Acesso especial → Exibir sobre outros apps → iFood ROI Tracker → Permitir
- Samsung: Configurações → Apps → iFood ROI Tracker → Aparecer no topo → Permitir

### "Não detecta ofertas"
1. Abra iFood Entregador → fique na tela inicial
2. Aguarde oferta aparecer
3. Se não detectar: force close dos dois apps → reabra iFood → reabra ROI Tracker
4. Verifique se o pacote é `br.com.ifood.deliver` (não `br.com.ifood`)

### "Valores errados"
- Parser usa regex + heurística; layouts diferentes podem confundir
- Me manda print da tela de oferta que eu ajusto o parser

---

## 🏗️ Arquitetura

```
├── app/
│   ├── src/main/
│   │   ├── java/com/ifoodroitracker/
│   │   │   ├── data/
│   │   │   │   ├── db/           # Room: Entity, DAO, Database
│   │   │   │   ├── entity/       # DeliveryOffer, ParsedOffer, Thresholds
│   │   │   │   ├── prefs/        # DataStore (thresholds, config)
│   │   │   │   └── repository/   # DeliveryRepository
│   │   │   ├── di/               # Hilt Modules
│   │   │   ├── service/
│   │   │   │   ├── IfoodAccessibilityService  # Lê tela iFood
│   │   │   │   ├── IfoodScreenParser          # Extrai dados (regex + heurística)
│   │   │   │   ├── DecisionEngine             # Avalia se vale a pena
│   │   │   │   └── OverlayManager             # Bolha flutuante (ComposeView)
│   │   │   ├── ui/
│   │   │   │   ├── overlay/      # OverlayContent (Compose)
│   │   │   │   ├── viewmodel/    # Main, Settings, History, Stats
│   │   │   │   ├── MainActivity  # NavHost + BottomBar
│   │   │   │   ├── SettingsScreen
│   │   │   │   ├── HistoryScreen
│   │   │   │   └── StatsScreen
│   │   │   ├── receiver/         # BootReceiver
│   │   │   └── IfoodRoiApplication
│   │   ├── res/
│   │   │   ├── xml/              # accessibility_service_config, file_provider_paths
│   │   │   ├── values/           # strings, colors, themes
│   │   │   └── drawable/         # Icons
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── .github/workflows/build.yml   # CI/CD
├── settings.gradle.kts
├── build.gradle.kts
└── README.md
```

**Stack**: Kotlin, Jetpack Compose, Room, DataStore, Hilt, Coroutines, Flow, Material3

---

## 🤝 Contribuindo

1. Fork → Branch `feature/nome` → PR
2. Issues: Bug reports, feature requests, parser improvements
3. Testes em diferentes dispositivos/versões Android são bem-vindos

---

## 📄 Licença

MIT License - Use livremente, modifique, distribua.

---

## 🙋 Suporte

- **Issues**: [GitHub Issues](https://github.com/seu-usuario/ifood-roi-tracker/issues)
- **Discussões**: [GitHub Discussions](https://github.com/seu-usuario/ifood-roi-tracker/discussions)

---

## ⭐ Se Ajudou

Deixa uma ⭐ no repo! Ajuda outros entregadores a encontrarem.

---

**Desenvolvido por entregador, para entregador** 🛵💨

> "Não aceite no escuro. Saiba exatamente quanto vai ganhar por km e por hora."