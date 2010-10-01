/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Gunter Zeilinger, Huetteldorferstr. 24/10, 1150 Vienna/Austria/Europe.
 * Portions created by the Initial Developer are Copyright (C) 2010
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunterze@gmail.com>
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */

package org.dcm4che.data;

import java.io.UnsupportedEncodingException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.StringTokenizer;

public class SpecificCharacterSet {
    
    public static final SpecificCharacterSet DEFAULT =
            new SpecificCharacterSet(Codec.ISO_646);

    private static ThreadLocal<SoftReference<Encoder>> cachedEncoder1 = 
            new ThreadLocal<SoftReference<Encoder>>();

    private static ThreadLocal<SoftReference<Encoder>> cachedEncoder2 = 
            new ThreadLocal<SoftReference<Encoder>>();

    protected final Codec[] codecs;

    private enum Codec {
        ISO_646("US-ASCII", 0x2842, 0),
        ISO_8859_1("ISO-8859-1", 0x2842, 0x2d41),
        ISO_8859_2("ISO-8859-2", 0x2842, 0x2d42),
        ISO_8859_3("ISO-8859-3", 0x2842, 0x2d43),
        ISO_8859_4("ISO-8859-4", 0x2842, 0x2d44),
        ISO_8859_5("ISO-8859-5", 0x2842, 0x2d4c),
        ISO_8859_6("ISO-8859-6", 0x2842, 0x2d47),
        ISO_8859_7("ISO-8859-7", 0x2842, 0x2d46),
        ISO_8859_8("ISO-8859-8", 0x2842, 0x2d48),
        ISO_8859_9("ISO-8859-9", 0x2842, 0x2d4d),
        JIS_X_201("JIS_X0201", 0x284a, 0x2949),
        TIS_620("TIS-620", 0x2842, 0x2d54),
        JIS_X_208("x-JIS0208", -1, 0x2442),
        JIS_X_212("JIS_X0212-1990", -1, 0x242844),
        KS_X_1001("EUC-KR", 0, 0x242943),
        UTF_8("UTF-8", 0, 0),
        GB18030("GB18030", 0, 0);
        
        private final String charsetName;
        private final int escSeq0;
        private final int escSeq1;

        private Codec(String charsetName, int escSeq0, int escSeq1) {
            this.charsetName = charsetName;
            this.escSeq0 = escSeq0;
            this.escSeq1 = escSeq1;
        }

        public static Codec forCode(String code) {
            if (code == null)
                return ISO_646;
                
            switch (code.hashCode()) {
            case -332223596: // "ISO_IR 100".hashCode()
                if ("ISO_IR 100".equals(code))
                    return Codec.ISO_8859_1;
                break;
            case -332223595: // "ISO_IR 101".hashCode()
                if ("ISO_IR 101".equals(code))
                    return Codec.ISO_8859_2;
                break;
            case -332223587: // "ISO_IR 109".hashCode()
                if ("ISO_IR 109".equals(code))
                    return Codec.ISO_8859_3;
                break;
            case -332223565: // "ISO_IR 110".hashCode()
                if ("ISO_IR 110".equals(code))
                    return Codec.ISO_8859_4;
                break;
            case -332223468: // "ISO_IR 144".hashCode()
                if ("ISO_IR 144".equals(code))
                    return Codec.ISO_8859_5;
                break;
            case -332223527: // "ISO_IR 127".hashCode()
                if ("ISO_IR 127".equals(code))
                    return Codec.ISO_8859_6;
                break;
            case -332223528: // "ISO_IR 126".hashCode()
                if ("ISO_IR 126".equals(code))
                    return Codec.ISO_8859_7;
                break;
            case -332223495: // "ISO_IR 138".hashCode()
                if ("ISO_IR 138".equals(code))
                    return Codec.ISO_8859_8;
                break;
            case -332223464: // "ISO_IR 148".hashCode()
                if ("ISO_IR 148".equals(code))
                    return Codec.ISO_8859_9;
                break;
            case -287811553: // "ISO_IR 13".hashCode()
                if ("ISO_IR 13".equals(code))
                    return Codec.JIS_X_201;
                break;
            case -332223404: // "ISO_IR 166".hashCode()
                if ("ISO_IR 166".equals(code))
                    return Codec.TIS_620;
                break;
            case 427562726: // "ISO 2022 IR 6".hashCode()
                if ("ISO 2022 IR 6".equals(code))
                    return Codec.ISO_646;
                break;
            case -1429083999: // "ISO 2022 IR 100".hashCode()
                if ("ISO 2022 IR 100".equals(code))
                    return Codec.ISO_8859_1;
                break;
            case -1429083998: // "ISO 2022 IR 101".hashCode()
                if ("ISO 2022 IR 101".equals(code))
                    return Codec.ISO_8859_2;
                break;
            case -1429083990: // "ISO 2022 IR 109".hashCode()
                if ("ISO 2022 IR 109".equals(code))
                    return Codec.ISO_8859_3;
                break;
            case -1429083968: // "ISO 2022 IR 110".hashCode()
                if ("ISO 2022 IR 110".equals(code))
                    return Codec.ISO_8859_4;
                break;
            case -1429083871: // "ISO 2022 IR 144".hashCode()
                if ("ISO 2022 IR 144".equals(code))
                    return Codec.ISO_8859_5;
                break;
            case -1429083930: // "ISO 2022 IR 127".hashCode()
                if ("ISO 2022 IR 127".equals(code))
                    return Codec.ISO_8859_6;
                break;
            case -1429083931: // "ISO 2022 IR 126".hashCode()
                if ("ISO 2022 IR 126".equals(code))
                    return Codec.ISO_8859_7;
                break;
            case -1429083898: // "ISO 2022 IR 138".hashCode()
                if ("ISO 2022 IR 138".equals(code))
                    return Codec.ISO_8859_8;
                break;
            case -1429083867: // "ISO 2022 IR 148".hashCode()
                if ("ISO 2022 IR 148".equals(code))
                    return Codec.ISO_8859_9;
                break;
            case 369542514: // "ISO 2022 IR 13".hashCode()
                if ("ISO 2022 IR 13".equals(code))
                    return Codec.JIS_X_201;
                break;
            case -1429083807: // "ISO 2022 IR 166".hashCode()
                if ("ISO 2022 IR 166".equals(code))
                    return Codec.TIS_620;
                break;
            case 369542735: // "ISO 2022 IR 87".hashCode()
                if ("ISO 2022 IR 87".equals(code))
                    return Codec.JIS_X_208;
                break;
            case -1429083835: // "ISO 2022 IR 159".hashCode()
                if ("ISO 2022 IR 159".equals(code))
                    return Codec.JIS_X_212;
                break;
            case -1429083866: // "ISO 2022 IR 149".hashCode()
                if ("ISO 2022 IR 149".equals(code))
                    return Codec.KS_X_1001;
                break;
            case -332223315: // "ISO_IR 192".hashCode()
                if ("ISO_IR 192".equals(code))
                    return Codec.UTF_8;
                break;
            case 524744459: // "GB18030".hashCode()
                if ("GB18030".equals(code))
                    return Codec.GB18030;
                break;
            }
            return ISO_646;
        }

        public byte[] encode(String val) {
            try {
                return val.getBytes(charsetName);
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
        }

        public String decode(byte[] b, int off, int len) {
            try {
                return new String(b, off, len, charsetName);
            } catch (UnsupportedEncodingException e) {
                throw new AssertionError(e);
            }
        }

        public boolean containsASCII() {
            return escSeq0 >= 0;
        }

        public int getEscSeq0() {
            return escSeq0;
        }

        public int getEscSeq1() {
            return escSeq1;
        }

    }

    private static final class Encoder {
        final Codec codec;
        final CharsetEncoder encoder;
 
        public Encoder(Codec codec) {
            this.codec = codec;
            this.encoder = Charset.forName(codec.charsetName).newEncoder();
        }

        public boolean encode(CharBuffer cb, ByteBuffer bb, boolean escSeq,
                CodingErrorAction errorAction) {
            encoder.onMalformedInput(errorAction)
                    .onUnmappableCharacter(errorAction)
                    .reset();
            int cbmark = cb.position();
            int bbmark = bb.position();
            try {
                if (escSeq)
                    escSeq(bb, codec.getEscSeq1());
                CoderResult cr = encoder.encode(cb, bb, true);
                if (!cr.isUnderflow())
                    cr.throwException();
                cr = encoder.flush(bb);
                if (!cr.isUnderflow())
                    cr.throwException();
            } catch (CharacterCodingException x) {
                cb.position(cbmark);
                bb.position(bbmark);
                return false;
            }
            return true;
        }

        private static void escSeq(ByteBuffer bb, int seq) {
            bb.put((byte) 0x1b);
            int b1 = seq >> 16;
            if (b1 != 0)
                bb.put((byte) b1);
            bb.put((byte) (seq >> 8));
            bb.put((byte) seq);
        }
    }

    private static final class ISO2022 extends SpecificCharacterSet {

        private ISO2022(Codec... charsetInfos) {
            super(charsetInfos);
        }

        @Override
        public byte[] encode(String val, String delimiters) {
            int strlen = val.length();
            CharBuffer cb = CharBuffer.wrap(val.toCharArray());
            Encoder enc1 = encoder(cachedEncoder1, codecs[0]);
            byte[] buf = new byte[strlen];
            ByteBuffer bb = ByteBuffer.wrap(buf);
            // try to encode whole string value with character set specified
            // by value1 of (0008,0005) Specific Character Set
            if (!enc1.encode(cb, bb, false, CodingErrorAction.REPORT)) {
                // split whole string value according VR specific delimiters
                // and try to encode each component separately
                Encoder[] encs = new Encoder[codecs.length];
                encs[0] = enc1;
                encs[1] = encoder(cachedEncoder2, codecs[1]);
                StringTokenizer comps =
                        new StringTokenizer(val, delimiters, true);
                buf = new byte[2 * strlen + 4 * (comps.countTokens() + 1)];
                bb = ByteBuffer.wrap(buf);
                int cur = 0;
                while (comps.hasMoreTokens()) {
                    String comp = comps.nextToken();
                    if (comp.length() == 1 // if delimiter
                            && delimiters.indexOf(comp.charAt(0)) >= 0) {
                        // switch to initial character set, if current active
                        // character set does not contain ASCII
                        if (!codecs[cur].containsASCII())
                            Encoder.escSeq(bb, codecs[0].getEscSeq0());
                        bb.put((byte) comp.charAt(0));
                        cur = 0;
                        continue;
                    }
                    cb = CharBuffer.wrap(comp.toCharArray());
                    // try to encode component with current active character set
                    if (encs[cur].encode(cb, bb, false,
                            CodingErrorAction.REPORT))
                        continue;
                    int next = cur;
                    // try to encode component with other character sets
                    // specified by values of (0008,0005) Specific Character Set
                    do {
                        next = (next + 1) % encs.length;
                        if (next == cur) {
                            // component could not be encoded with any of the
                            // specified character sets, encode it with the
                            // initial character set, using the default
                            // replacement of the character set decoder
                            // for characters which cannot be encoded
                            if (!codecs[cur].containsASCII())
                                Encoder.escSeq(bb, codecs[0].getEscSeq0());
                            encs[0].encode(cb, bb, false,
                                    CodingErrorAction.REPLACE);
                            next = 0;
                            break;
                        }
                        if (encs[next] == null)
                            encs[next] = new Encoder(codecs[next]);
                    } while (!encs[next].encode(cb, bb, true, 
                            CodingErrorAction.REPORT));
                    cur = next;
                }
                if (!codecs[cur].containsASCII())
                    Encoder.escSeq(bb, codecs[0].getEscSeq0());
            }
            return Arrays.copyOf(buf, bb.position());
        }

        @Override
        public String decode(byte[] b) {
            Codec codec = codecs[0];
            int off = 0;
            int cur = 0;
            int step = 1;
            StringBuffer sb = new StringBuffer(b.length);
            while (cur < b.length) {
                if (b[cur] == 0x1b) { // ESC
                    if (off < cur) {
                        sb.append(codec.decode(b, off, cur - off));
                    }
                    cur += 3;
                    switch (((b[cur - 2] & 255) << 8) + (b[cur - 1] & 255)) {
                    case 0x2428:
                        if (b[cur++] == 0x44) {
                            codec = Codec.JIS_X_212;
                            step = 2;
                        } else { // decode invalid ESC sequence as chars
                            sb.append(codec.decode(b, cur - 4, 4));
                        }
                        break;
                    case 0x2429:
                        if (b[cur++] == 0x43) {
                            codec = Codec.KS_X_1001;
                            step = -1;
                        } else { // decode invalid ESC sequence as chars
                            sb.append(codec.decode(b, cur - 4, 4));
                        }
                        break;
                    case 0x2442:
                        codec = Codec.JIS_X_208;
                        step = 2;
                        break;
                    case 0x2842:
                        codec = Codec.ISO_646;
                        step = 1;
                        break;
                    case 0x284a:
                    case 0x2949:
                        codec = Codec.JIS_X_201;
                        step = 1;
                        break;
                    case 0x2d41:
                        codec = Codec.ISO_8859_1;
                        step = 1;
                        break;
                    case 0x2d42:
                        codec = Codec.ISO_8859_2;
                        step = 1;
                        break;
                    case 0x2d43:
                        codec = Codec.ISO_8859_3;
                        step = 1;
                        break;
                    case 0x2d44:
                        codec = Codec.ISO_8859_4;
                        step = 1;
                        break;
                    case 0x2d46:
                        codec = Codec.ISO_8859_7;
                        step = 1;
                        break;
                    case 0x2d47:
                        codec = Codec.ISO_8859_6;
                        step = 1;
                        break;
                    case 0x2d48:
                        codec = Codec.ISO_8859_8;
                        step = 1;
                        break;
                    case 0x2d4c:
                        codec = Codec.ISO_8859_5;
                        step = 1;
                        break;
                    case 0x2d4d:
                        codec = Codec.ISO_8859_9;
                        step = 1;
                        break;
                    case 0x2d54:
                        codec = Codec.TIS_620;
                        step = 1;
                        break;
                    default: // decode invalid ESC sequence as chars
                        sb.append(codec.decode(b, cur - 3, 3));
                    }
                    off = cur;
                } else {
                    cur += step > 0 ? step : b[cur] < 0 ? 2 : 1;
                }
            }
            if (off < cur) {
                sb.append(codec.decode(b, off, cur - off));
            }
            return sb.toString();
        }
    }

    public static SpecificCharacterSet valueOf(String[] codes) {
        if (codes == null || codes.length == 0)
            return DEFAULT;

        Codec[] infos = new Codec[codes.length];
        for (int i = 0; i < codes.length; i++)
            infos[i] = Codec.forCode(codes[i]);
        return codes.length > 1 ? new ISO2022(infos)
                : new SpecificCharacterSet(infos);
    }

    private static Encoder encoder(ThreadLocal<SoftReference<Encoder>> tl,
            Codec codec) {
        SoftReference<Encoder> sr;
        Encoder enc;
        if ((sr = tl.get()) == null || (enc = sr.get()) == null
                || enc.codec != codec)
            tl.set(new SoftReference<Encoder>(enc = new Encoder(codec)));
        return enc;
    }

    protected SpecificCharacterSet(Codec... codecs) {
        this.codecs = codecs;
    }

    public byte[] encode(String val, String delimiters) {
        return codecs[0].encode(val);
    }

    public String decode(byte[] val) {
        return codecs[0].decode(val, 0, val.length);
    }

}
