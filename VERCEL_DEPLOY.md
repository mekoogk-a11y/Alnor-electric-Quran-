# النشر على Vercel — مصحف النور الإلكتروني

تمت تهيئة المشروع ليعمل على **Vercel** مباشرة بدون أخطاء، جنباً إلى جنب مع تطبيق **Android** الأصلي في مجلد `app/`.

## ما تم إضافته وتصحيحه للنشر على Vercel

1. **`vercel.json`**
   - تعيين `"framework": null` و `"outputDirectory": "public"` لمنع خطأ `No Output Directory named "public" found`.
   - ضبط `"maxDuration": 10` المتوافق مع خطة Vercel المجانية (Hobby) والمدفوعة.
   - إعادة توجيه جميع مسارات `/api/quran/*` إلى دالة الخادم `/api/quran` مع ترويسات الأمان وCORS.
2. **`package.json`**
   - تحديد Node.js `>=20.0.0` بدون أي حزم خارجية معرضة لفشل التثبيت (`npm install`).
3. **`api/quran.mjs` و `api/health.mjs`**
   - تنفيذ كافة المسارات المطلوبة:
     - `GET /api/quran/chapters`
     - `GET /api/quran/chapters/:id`
     - `GET /api/quran/pages/:number`
     - `GET /api/quran/verses`
     - `GET /api/quran/tafsirs`
     - `GET /api/quran/translations`
     - `GET /api/quran/recitations`
     - `GET /api/quran/audio`
     - `GET /api/quran/search`
     - `GET /api/health`
   - دعم مصادقة **OAuth2 Client Credentials** (`QF_CLIENT_ID` و `QF_CLIENT_SECRET`) مع حفظ المؤقت للرمز (`token caching`) وتجديده قبل الانتهاء، والتحقق من المدخلات، وتحديد المعدل (`rate limiting`)، والرجوع الآمن لواجهة `api.quran.com/api/v4` إذا لم تُضبط مفاتيح OAuth2 بعد.
4. **`public/index.html`**
   - واجهة ويب عربية متجاوبة للمصحف الشريف (٦٠٤ صفحة + ١١٤ سورة + التلاوة الصوتية + البحث) مرتبطة مباشرة بمسارات `/api/quran/*`.

## خطوات النشر على Vercel

1. ادفع المشروع إلى مستودع GitHub.
2. استورد المستودع في **Vercel Dashboard**.
3. أبقِ **Root Directory** على `/` و **Framework Preset** على `Other` (سيقرأ Vercel ملف `vercel.json` تلقائياً).
4. *(اختياري لبيانات OAuth2 الخاصة)* من **Settings → Environment Variables** أضف المتغيرات الموضحة في `vercel.env.example`:
   - `QF_CLIENT_ID`
   - `QF_CLIENT_SECRET`
5. اضغط **Deploy**.
