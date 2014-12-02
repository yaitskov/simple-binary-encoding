package uk.co.real_logic.sbe.xml;

/**
* Daneel Yaitskov
*/
enum EncodingType
{
    uint8, uint16, uint32, uint64, int8, int16, int32, int64, _char;

    public static EncodingType parse(String s)
    {
        if (s.equals("char"))
        {
            return _char;
        }
        try
        {
            return valueOf(s);
        }
        catch (IllegalArgumentException e)
        {
            return null;
        }
    }
}
