package de.tubs.ibr.dtn.ruralexplorer.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import de.tubs.ibr.dtn.api.SDNV;

public class TypeLengthValue {
	
	private final static int TLV_BOOLEAN 	= 0;
	private final static int TLV_UINT64 	= 1;
	private final static int TLV_SINT64 	= 2;
	private final static int TLV_FIXED16 	= 3;
	private final static int TLV_FIXED32 	= 4;
	private final static int TLV_FIXED64 	= 5;
	private final static int TLV_FLOAT		= 6;
	private final static int TLV_DOUBLE		= 7;
	private final static int TLV_STRING		= 8;
	private final static int TLV_BYTES		= 9;

	public static class Decoder {
		private final DataInputStream mInput;
		
		public Decoder(DataInputStream in) {
			mInput = in;
		}
		
		/**
		 * Return the next TLV type
		 * @return the TLV type
		 */
		public int next() throws IOException {
			// return the TLV type
			return mInput.readUnsignedByte();
		}
		
		/**
		 * open the TLV structure and return its length
		 * @return The length of the TLV structure
		 */
		public int open() throws IOException {
			int len = SDNV.Read(mInput);
			return len;
		}
		
		public Object read() throws IOException {
			int type = mInput.readUnsignedByte();
			switch (type) {
				case TLV_BOOLEAN:
					return Boolean.valueOf(mInput.readUnsignedByte() == 1);
					
				case TLV_UINT64:
					return Integer.valueOf(SDNV.Read(mInput));
					
				case TLV_SINT64:
					return Integer.valueOf(SDNV.Read(mInput));
					
				case TLV_FIXED16:
					return Integer.valueOf(mInput.readShort());
					
				case TLV_FIXED32:
					return Integer.valueOf(mInput.readInt());
					
				case TLV_FIXED64:
					return Long.valueOf(mInput.readLong());
					
				case TLV_FLOAT:
					return Float.valueOf(mInput.readFloat());
					
				case TLV_DOUBLE:
					return Double.valueOf(mInput.readDouble());
					
				case TLV_STRING:
				{
					int len = SDNV.Read(mInput);
					byte[] buf = new byte[len];
					mInput.read(buf, 0, len);
					return new String(buf);
				}
					
				case TLV_BYTES:
				{
					int len = SDNV.Read(mInput);
					byte[] buf = new byte[len];
					mInput.read(buf, 0, len);
					return buf;
				}
			}
			return null;
		}
		
		public void skip() throws IOException {
			int len = open();
			mInput.skip(len);
		}
	}
	
	public static class Encoder {
		private final DataOutputStream mOutput;
		
		public Encoder(DataOutputStream out) {
			mOutput = out;
		}
		
		public void write(int type, int length) throws IOException {
			mOutput.writeByte(type);
			SDNV.Write(mOutput, length);
		}
		
		public void write(boolean value) throws IOException {
			mOutput.writeByte(TLV_BOOLEAN);
			mOutput.writeByte(value ? 1 : 0);
		}
		
		public void writeUint64(int value) throws IOException {
			mOutput.writeByte(TLV_UINT64);
			SDNV.Write(mOutput, value);
		}
		
		public void writeSint64(int value) throws IOException {
			mOutput.writeByte(TLV_SINT64);
			SDNV.Write(mOutput, value);
		}
		
		public void writeFixed16(int value) throws IOException {
			mOutput.writeByte(TLV_FIXED16);
			mOutput.writeShort(value);
		}
		
		public void writeFixed32(int value) throws IOException {
			mOutput.writeByte(TLV_FIXED32);
			mOutput.writeInt(value);
		}
		
		public void writeFixed64(long value) throws IOException {
			mOutput.writeByte(TLV_FIXED64);
			mOutput.writeLong(value);
		}
		
		public void write(float value) throws IOException {
			mOutput.writeByte(TLV_FLOAT);
			mOutput.writeFloat(value);
		}
		
		public void write(double value) throws IOException {
			mOutput.writeByte(TLV_DOUBLE);
			mOutput.writeDouble(value);
		}
		
		public void write(String value) throws IOException {
			mOutput.writeByte(TLV_STRING);
			SDNV.Write(mOutput, value.length());
			mOutput.writeBytes(value);
		}
		
		public void write(byte[] data) throws IOException {
			mOutput.writeByte(TLV_BYTES);
			SDNV.Write(mOutput, data.length);
			mOutput.write(data);
		}
	}
}
