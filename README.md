# iOZ Bank Tycoon — Android APK Kurulumu

Bu depo, `www/index.html` içindeki tek dosyalık HTML oyununu **Capacitor** ile sarmalayıp GitHub Actions üzerinden otomatik olarak Android APK'sına dönüştürür. Reklamlar için **Unity Ads** (ödüllü reklam) native köprüsü hazır durumda.

## Klasör yapısı

```
ioz-bank-tycoon/
├── www/index.html              → Oyunun kendisi (tek dosya, görseller gömülü)
├── capacitor.config.json       → Uygulama kimliği: com.yapayzekapolat1.bankakur
├── package.json                → Capacitor bağımlılıkları
├── resources/icon.png          → Uygulama ikonu (1024x1024, otomatik tüm boyutlara dönüştürülür)
├── patches/MainActivity.java   → Unity Ads'i başlatan ve JS köprüsünü açan native kod
├── privacy-policy.html         → Play Store için gizlilik politikası (GitHub Pages'te yayınlanabilir)
└── .github/workflows/build-apk.yml → Her push'ta otomatik APK derleyen iş akışı
```

## Unity Ads bilgileri (zaten koda işlendi)

| Alan | Değer |
|---|---|
| Paket adı (appId) | `com.yapayzekapolat1.bankakur` |
| Game ID | `800362657` |
| Placement (Rewarded) | `Rewarded_Android` |
| Organization Core ID | `18968483151964` |

Bu değerler `patches/MainActivity.java` içinde tanımlı. Değişirse sadece o dosyadaki üç sabiti güncellemen yeterli.

## GitHub'a nasıl yüklenir

1. GitHub'da yeni, **boş** bir repo oluştur (örn. `ioz-bank-tycoon`) — README/gitignore eklemeden.
2. Bu klasörün içindeki her şeyi o reponun içine kopyala.
3. Şu komutlarla push et:
   ```bash
   cd ioz-bank-tycoon
   git init
   git add .
   git commit -m "İlk yükleme"
   git branch -M main
   git remote add origin https://github.com/KULLANICI_ADIN/ioz-bank-tycoon.git
   git push -u origin main
   ```
4. Push işleminden birkaç saniye sonra **Actions** sekmesine git — "APK Derle" iş akışı otomatik başlayacak (5-10 dakika sürer).
5. İş akışı bittiğinde, o çalıştırmanın sayfasında **Artifacts** bölümünden `ioz-bank-tycoon-debug-apk` dosyasını indirebilirsin — bu senin APK'n.

İstediğin zaman elle de tetikleyebilirsin: **Actions → APK Derle → Run workflow**.

## Önemli: İki farklı derleme var

- **`build-apk.yml`** (her push'ta otomatik çalışır) → **debug** APK üretir. Telefonuna kurup test etmek için yeterli, Play Store bunu kabul etmez.
- **`release-build.yml`** (sadece elle tetiklenir) → **imzalı release** AAB (Play Store'a yüklenecek dosya) + imzalı test APK'sı üretir.

## İmzalı release nasıl alınır

1. Sana hazır bir keystore (imzalama anahtarı) ve parolalarını ayrı bir dosyada gönderdim — **KEYSTORE_BILGILERI.txt**. Bu dosyayı ve `release.keystore.base64.txt` dosyasını çok güvenli bir yerde sakla (parola yöneticisi vb). Kaybedersen uygulamayı bir daha güncelleyemezsin.
2. GitHub reponda **Settings → Secrets and variables → Actions → New repository secret** yoluyla şu 4 secret'ı ekle (değerleri KEYSTORE_BILGILERI.txt içinde):
   - `KEYSTORE_BASE64`
   - `KEYSTORE_PASSWORD`
   - `KEY_ALIAS`
   - `KEY_PASSWORD`
3. **Actions → İmzalı Release Derle → Run workflow** ile elle tetikle.
4. Birkaç dakika sonra Artifacts kısmında `ioz-bank-tycoon-release` içinde hem `.aab` (Play Console'a yükleyeceğin dosya) hem de imzalı `.apk` (kendi telefonunda test için) olacak.

İstersen kendi keystore'unu da oluşturabilirsin:
```bash
keytool -genkeypair -v -keystore release.keystore -alias ioz-bank \
  -keyalg RSA -keysize 2048 -validity 10000
```

## Gizlilik politikası

`privacy-policy.html` dosyasını **GitHub Pages** ile ücretsiz yayınlayabilirsin:

1. Repo **Settings → Pages**
2. Source: `main` branch, `/ (root)` klasörü
3. Birkaç dakika sonra adresin şu şekilde olur:
   `https://KULLANICI_ADIN.github.io/ioz-bank-tycoon/privacy-policy.html`
4. Dosyanın içindeki `[GELİŞTİRİCİ ADINIZI BURAYA YAZIN]` ve `[E-POSTA ADRESİNİZİ BURAYA YAZIN]` kısımlarını kendi bilgilerinle değiştirmeyi unutma.
5. Bu linki Play Console'daki "Gizlilik Politikası" alanına yapıştıracaksın.

## Test modu hakkında not

`patches/MainActivity.java` içinde `TEST_MODE = false` olarak ayarlı — yani gerçek reklamlar gösterilir. Geliştirme sırasında sahte/test reklamlarını görmek istersen bu değeri geçici olarak `true` yapabilirsin, **ama Play Store'a yüklemeden önce mutlaka `false`'a geri almalısın.**

## Oyun tarafında reklam mantığı (zaten hazır)

- **Yıl sonu:** Ödüllü reklam izlenmeden yeni yıla geçilemiyor.
- **"$10.000 Kazan" butonu:** Bakiyenin yanında, dashboard ekranında görünür, en fazla 3 kez kullanılabilir, her kullanımda $10.000 bakiyeye eklenir. Kalan hak ilerlemeyle birlikte kaydedilir.

Bu iki özellik `window.AndroidAds.showRewardedAd()` üzerinden native tarafa (Unity Ads) bağlanıyor; tarayıcıda test ederken native köprü bulunmadığından ödül otomatik simüle edilir.

## Sırada ne var?

- [ ] Keystore oluşturup release/AAB imzalama adımını workflow'a eklemek
- [ ] Play Console'da uygulamayı oluşturup mağaza görsellerini (ekran görüntüleri, öne çıkan görsel) hazırlamak
- [ ] Gizlilik politikasındaki geliştirici adı/e-posta alanlarını doldurmak
- [ ] İçerik derecelendirme anketini doldurmak (reklam + para teması olduğu için "Kumar" değil "Simülasyon" kategorisinde ilerlemek mantıklı)
