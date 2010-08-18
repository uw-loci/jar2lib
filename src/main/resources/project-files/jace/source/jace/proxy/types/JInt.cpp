
#include "jace/proxy/types/JInt.h"

#ifndef JACE_JCLASS_IMPL_H
#include "jace/JClassImpl.h"
#endif
using jace::JClassImpl;

#include <iostream>
using std::ostream;

#include "jace/BoostWarningOff.h"
#include <boost/thread/mutex.hpp>
#include "jace/BoostWarningOn.h"

BEGIN_NAMESPACE_3( jace, proxy, types )


JInt::JInt( jvalue value )
{
  setJavaJniValue( value );
}

JInt::JInt( const jint int_ )
{
  jvalue value;
  value.i = int_;
  setJavaJniValue( value );
}

JInt::JInt( const JByte& byte_ )
{
  jvalue value;
  value.i = byte_.getByte();
  setJavaJniValue( value );
}


JInt::~JInt()
{}

JInt::operator jint()
{ 
  return getJavaJniValue().i; 
}

jint JInt::getInt() const
{
  return getJavaJniValue().i;
}

bool JInt::operator==( const JInt& int_ ) const
{
  return int_.getInt() == getInt();
}

bool JInt::operator!=( const JInt& int_ ) const
{
  return !( *this == int_ );
}

bool JInt::operator==( jint val ) const
{
  return val == getInt();
}

bool JInt::operator!=( jint val ) const
{
  return ! ( *this == val );
}

static boost::mutex javaClassMutex;
const JClass& JInt::staticGetJavaJniClass() throw ( JNIException )
{
	static boost::shared_ptr<JClassImpl> result;
	boost::mutex::scoped_lock lock(javaClassMutex);
	if (result == 0)
		result = boost::shared_ptr<JClassImpl>(new JClassImpl("int", "I"));
	return *result;
}

const JClass& JInt::getJavaJniClass() const throw ( JNIException )
{
  return JInt::staticGetJavaJniClass();
}

ostream& operator<<( ostream& stream, const JInt& val )
{
  return stream << val.getInt();
}

END_NAMESPACE_3( jace, proxy, types )

