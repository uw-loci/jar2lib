
#include "jace/proxy/types/JByte.h"

#ifndef JACE_JCLASS_IMPL_H
#include "jace/JClassImpl.h"
#endif
using jace::JClassImpl;

#include "jace/BoostWarningOff.h"
#include <boost/thread/mutex.hpp>
#include "jace/BoostWarningOn.h"

BEGIN_NAMESPACE_3( jace, proxy, types )

JByte::JByte( jvalue value )
{
  setJavaJniValue( value );
}

JByte::JByte( jbyte byte )
{
  jvalue value;
  value.b = byte;
  setJavaJniValue( value );
}

JByte::~JByte()
{}

JByte::operator jbyte() const
{ 
  return getJavaJniValue().b;
}

jbyte JByte::getByte() const
{
  return getJavaJniValue().b;
}

bool JByte::operator==( const JByte& byte_ ) const
{
  return byte_.getByte() == getByte();
}

bool JByte::operator!=( const JByte& byte_ ) const
{
  return !( *this == byte_ );
}

bool JByte::operator==( jbyte val ) const
{
  return val == getByte();
}

bool JByte::operator!=( jbyte val ) const
{
  return ! ( *this == val );
}

static boost::mutex javaClassMutex;
const JClass& JByte::staticGetJavaJniClass() throw ( JNIException )
{
	static boost::shared_ptr<JClassImpl> result;
	boost::mutex::scoped_lock lock(javaClassMutex);
	if (result == 0)
		result = boost::shared_ptr<JClassImpl>(new JClassImpl("byte", "B"));
	return *result;
}

const JClass& JByte::getJavaJniClass() const throw ( JNIException )
{
  return JByte::staticGetJavaJniClass();
}


END_NAMESPACE_3( jace, proxy, types )

